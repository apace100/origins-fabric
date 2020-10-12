package io.github.apace100.origins.power;

import io.github.apace100.origins.mixin.EyeHeightAccess;
import io.github.apace100.origins.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

public class FireProjectilePower extends ActiveCooldownPower {

    private final EntityType entityType;
    private final int projectileCount;
    private final float speed;
    private final float divergence;
    private final SoundEvent soundEvent;

    public FireProjectilePower(PowerType<?> type, PlayerEntity player, int cooldownDuration, HudRender hudRender, EntityType entityType, int projectileCount, float speed, float divergence, SoundEvent soundEvent) {
        super(type, player, cooldownDuration, hudRender, null);
        this.entityType = entityType;
        this.projectileCount = projectileCount;
        this.speed = speed;
        this.divergence = divergence;
        this.soundEvent = soundEvent;
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
            Vec3d spawnPos = player.getPos().add(0, ((EyeHeightAccess)player).callGetEyeHeight(player.getPose(), player.getDimensions(player.getPose())), 0).add(player.getRotationVector());
            entity.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
            if(entity instanceof ProjectileEntity) {
                ProjectileEntity projectile = (ProjectileEntity)entity;
                projectile.setOwner(player);
                projectile.setProperties(player, player.pitch, player.yaw, 0F, speed, divergence);
            }
            player.world.spawnEntity(entity);
        }
    }
}
