package io.github.apace100.origins.mixin;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.*;
import io.github.apace100.origins.registry.ModBlocks;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow protected abstract float getJumpVelocity();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    // ELYTRA
    @Redirect(method = "initAi", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setFlag(IZ)V"))
    private void preventStoppingToFly(LivingEntity livingEntity, int index, boolean value) {
        if(this.getFlag(7) && !value && index == 7 && !this.onGround && !this.hasVehicle()
            && PowerTypes.ELYTRA.isActive(livingEntity) && !livingEntity.hasStatusEffect(StatusEffects.LEVITATION)) {
            this.setFlag(index, true);
        }
    }

    // SetEntityGroupPower
    @Inject(at = @At("HEAD"), method = "getGroup", cancellable = true)
    public void getGroup(CallbackInfoReturnable<EntityGroup> info) {
        if((Object)this instanceof PlayerEntity) {
            OriginComponent component = ModComponents.ORIGIN.get(this);
            List<SetEntityGroupPower> groups = component.getPowers(SetEntityGroupPower.class);
            if(groups.size() > 0) {
                if(groups.size() > 1) {
                    Origins.LOGGER.warn("Player " + this.getDisplayName().toString() + " had two instances of SetEntityGroupPower.");
                }
                info.setReturnValue(groups.get(0).group);
            }
        }
    }

    // SPRINT_JUMP
    @Inject(at = @At("HEAD"), method = "getJumpVelocity", cancellable = true)
    private void modifyJumpVelocity(CallbackInfoReturnable<Float> info) {
        if(this.isSprinting() && PowerTypes.SPRINT_JUMP.isActive(this)) {
            float vanilla = 0.42F * this.getJumpVelocityMultiplier();
            vanilla *= 1.5F;
            info.setReturnValue(vanilla);
        }
    }

    // HOTBLOODED
    @Inject(at = @At("HEAD"), method= "canHaveStatusEffect", cancellable = true)
    private void preventStatusEffects(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> info) {
        if(PowerTypes.HOTBLOODED.isActive(this) && (effect.getEffectType() == StatusEffects.POISON || effect.getEffectType() == StatusEffects.HUNGER)) {
            info.setReturnValue(false);
        }
    }

    // FIRE_IMMUNITY & FALL_IMMUNITY
    @Inject(at = @At("HEAD"), method = "damage", cancellable = true)
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if(source.isFire() && PowerTypes.FIRE_IMMUNITY.isActive(this)) {
            info.setReturnValue(false);
        }
        if(source == DamageSource.FALL && PowerTypes.FALL_IMMUNITY.isActive(this)) {
            info.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "damage", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;despawnCounter:I"), name = "amount")
    private float modifyDamageAmount(float originalAmount, DamageSource source, float amount) {
        if((Object)this instanceof PlayerEntity) {
            OriginComponent component = ModComponents.ORIGIN.get(this);
            float f = originalAmount;
            for (ModifyDamageTakenPower p : component.getPowers(ModifyDamageTakenPower.class)) {
                if (p.doesApply(source)) {
                    f = p.apply(f);
                }
            }
            return f;
        }
        return originalAmount;
    }

    // CLIMBING
    @Inject(at = @At("HEAD"), method = "isClimbing", cancellable = true)
    public void doSpiderClimbing(CallbackInfoReturnable<Boolean> info) {
        if(PowerTypes.CLIMBING.isActive(this) && PowerTypes.CLIMBING.get(this).isActive()) {
            if(this.horizontalCollision) {
                info.setReturnValue(true);
            }
        }
    }

    // WATER_BREATHING
    @Inject(at = @At("HEAD"), method = "canBreatheInWater", cancellable = true)
    public void doWaterBreathing(CallbackInfoReturnable<Boolean> info) {
        if(PowerTypes.WATER_BREATHING.isActive(this)) {
            info.setReturnValue(true);
        }
    }

    // SWIM_SPEED
    @ModifyConstant(method = "travel", constant = @Constant(floatValue = 0.02F, ordinal = 0))
    public float modifyBaseUnderwaterSpeed(float in) {
        if(PowerTypes.SWIM_SPEED.isActive(this)) {
            return in + PowerTypes.SWIM_SPEED.get(this).value;
        }
        return in;
    }

    // LIKE_WATER
    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;method_26317(DZLnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d method_26317Proxy(LivingEntity entity, double d, boolean bl, Vec3d vec3d) {
        Vec3d oldReturn = entity.method_26317(d, bl, vec3d);
        if(PowerTypes.LIKE_WATER.isActive(this)) {
            if (Math.abs(vec3d.y - d / 16.0D) < 0.025D) {
                return new Vec3d(oldReturn.x, 0, oldReturn.z);
            }
        }
        return entity.method_26317(d, bl, vec3d);
    }

    // SLOW_FALLING
    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), method = "travel", name = "d", ordinal = 0)
    public double doAvianSlowFalling(double in) {
        if(!this.isSneaking() && this.getVelocity().y <= 0.0D && PowerTypes.SLOW_FALLING.isActive(this)) {
            this.fallDistance = 0;
            return 0.01D;
        }
        return in;
    }

    // WEBBING
    @Inject(at = @At("HEAD"), method = "onAttacking")
    public void onAttacking(Entity target, CallbackInfo info) {
        if(target instanceof LivingEntity) {
            if(PowerTypes.WEBBING.isActive(this) && !this.isSneaking()) {
                CooldownPower power = PowerTypes.WEBBING.get(this);
                if(power.canUse()) {
                    BlockPos targetPos = target.getBlockPos();
                    if(world.isAir(targetPos) || world.getBlockState(targetPos).getMaterial().isReplaceable()) {
                        world.setBlockState(targetPos, ModBlocks.TEMPORARY_COBWEB.getDefaultState());
                        power.use();
                    }
                }
            }
        }
    }
}
