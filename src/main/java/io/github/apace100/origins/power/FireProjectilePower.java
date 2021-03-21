package io.github.apace100.origins.power;

import io.github.apace100.origins.mixin.EyeHeightAccess;
import io.github.apace100.origins.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FireProjectilePower extends ActiveCooldownPower {

    private final EntityType entityType;
    private final int projectileCount;
    private final float speed;
    private final float divergence;
    private final SoundEvent soundEvent;
    private final CompoundTag tag;

    public FireProjectilePower(PowerType<?> type, PlayerEntity player, int cooldownDuration, HudRender hudRender, EntityType entityType, int projectileCount, float speed, float divergence, SoundEvent soundEvent, CompoundTag tag) {
        super(type, player, cooldownDuration, hudRender, null);
        this.entityType = entityType;
        this.projectileCount = projectileCount;
        this.speed = speed;
        this.divergence = divergence;
        this.soundEvent = soundEvent;
        this.tag = tag;
    }

    @Override
    public void onUse() {
        if(canUse()) {
            fireProjectiles();
            use();
        }
    }

    private void fireProjectiles() {
        if(soundEvent != null) {
            player.world.playSound((PlayerEntity)null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundCategory.NEUTRAL, 0.5F, 0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
        }
        if (!player.world.isClient) {
            for(int i = 0; i < projectileCount; i++) {
                fireProjectile();
            }
        }
    }

    private void fireProjectile() {
        if(entityType != null) {
            Entity entity = entityType.create(player.world);
            if(entity == null) {
                return;
            }
            Vec3d rotationVector = player.getRotationVector();
            Vec3d spawnPos = player.getPos().add(0, ((EyeHeightAccess)player).callGetEyeHeight(player.getPose(), player.getDimensions(player.getPose())), 0).add(rotationVector);
            entity.refreshPositionAndAngles(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), player.pitch, player.yaw);
            if(entity instanceof ProjectileEntity) {
                if(entity instanceof ExplosiveProjectileEntity) {
                    ExplosiveProjectileEntity explosiveProjectileEntity = (ExplosiveProjectileEntity)entity;
                    explosiveProjectileEntity.posX = rotationVector.x * speed;
                    explosiveProjectileEntity.posY = rotationVector.y * speed;
                    explosiveProjectileEntity.posZ = rotationVector.z * speed;
                }
                ProjectileEntity projectile = (ProjectileEntity)entity;
                projectile.setOwner(player);
                projectile.setProperties(player, player.pitch, player.yaw, 0F, speed, divergence);
            } else {
                float f = -MathHelper.sin(player.yaw * 0.017453292F) * MathHelper.cos(player.pitch * 0.017453292F);
                float g = -MathHelper.sin(player.pitch * 0.017453292F);
                float h = MathHelper.cos(player.yaw * 0.017453292F) * MathHelper.cos(player.pitch * 0.017453292F);
                Vec3d vec3d = (new Vec3d(f, g, h)).normalize().add(player.getRandom().nextGaussian() * 0.007499999832361937D * (double)divergence, player.getRandom().nextGaussian() * 0.007499999832361937D * (double)divergence, player.getRandom().nextGaussian() * 0.007499999832361937D * (double)divergence).multiply((double)speed);
                entity.setVelocity(vec3d);
                Vec3d playerVelo = player.getVelocity();
                entity.setVelocity(entity.getVelocity().add(playerVelo.x, player.isOnGround() ? 0.0D : playerVelo.y, playerVelo.z));
            }
            if(tag != null) {
                CompoundTag mergedTag = entity.toTag(new CompoundTag());
                mergedTag.copyFrom(tag);
                entity.fromTag(mergedTag);
            }
            player.world.spawnEntity(entity);
        }
    }
}
