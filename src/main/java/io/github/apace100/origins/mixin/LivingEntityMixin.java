package io.github.apace100.origins.mixin;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.*;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow protected abstract float getJumpVelocity();

    @Shadow public abstract float getMovementSpeed();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    private void modifyWalkableFluids(Fluid fluid, CallbackInfoReturnable<Boolean> info) {
        if(OriginComponent.getPowers(this, WalkOnFluidPower.class).stream().anyMatch(p -> fluid.isIn(p.getFluidTag()))) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void invokeHitActions(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue()) {
            OriginComponent.getPowers(this, SelfActionWhenHitPower.class).forEach(p -> p.whenHit(source, amount));
            OriginComponent.getPowers(this, AttackerActionWhenHitPower.class).forEach(p -> p.whenHit(source, amount));
            OriginComponent.getPowers(source.getAttacker(), SelfActionOnHitPower.class).forEach(p -> p.onHit((LivingEntity)(Object)this, source, amount));
            OriginComponent.getPowers(source.getAttacker(), TargetActionOnHitPower.class).forEach(p -> p.onHit((LivingEntity)(Object)this, source, amount));
        }
    }

    // ModifyLavaSpeedPower
    @ModifyConstant(method = "travel", constant = {
        @Constant(doubleValue = 0.5D, ordinal = 0),
        @Constant(doubleValue = 0.5D, ordinal = 1),
        @Constant(doubleValue = 0.5D, ordinal = 2)
    })
    private double modifyLavaSpeed(double original) {
        return OriginComponent.modify(this, ModifyLavaSpeedPower.class, original);
    }

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isWet()Z"))
    private boolean preventExtinguishingFromSwimming(LivingEntity livingEntity) {
        if(OriginComponent.hasPower(livingEntity, SwimmingPower.class) && livingEntity.isSwimming() && !(getFluidHeight(FluidTags.WATER) > 0)) {
            return false;
        }
        return livingEntity.isWet();
    }

    // SetEntityGroupPower
    @Inject(at = @At("HEAD"), method = "getGroup", cancellable = true)
    public void getGroup(CallbackInfoReturnable<EntityGroup> info) {
        if((Object)this instanceof PlayerEntity) {
            OriginComponent component = ModComponents.ORIGIN.get(this);
            List<SetEntityGroupPower> groups = component.getPowers(SetEntityGroupPower.class);
            if(groups.size() > 0) {
                if(groups.size() > 1) {
                    Origins.LOGGER.warn("Player " + this.getDisplayName().toString() + " has two instances of SetEntityGroupPower.");
                }
                info.setReturnValue(groups.get(0).group);
            }
        }
    }

    // SPRINT_JUMP
    @Inject(at = @At("HEAD"), method = "getJumpVelocity", cancellable = true)
    private void modifyJumpVelocity(CallbackInfoReturnable<Float> info) {
        float base = 0.42F * this.getJumpVelocityMultiplier();
        float modified = OriginComponent.modify(this, ModifyJumpPower.class, base, p -> {
            p.executeAction();
            return true;
        });
        info.setReturnValue(modified);
    }

    // HOTBLOODED
    @Inject(at = @At("HEAD"), method= "canHaveStatusEffect", cancellable = true)
    private void preventStatusEffects(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> info) {
        for (EffectImmunityPower power : OriginComponent.getPowers(this, EffectImmunityPower.class)) {
            if(power.doesApply(effect)) {
                info.setReturnValue(false);
                return;
            }
        }
    }

    // CLIMBING
    @Inject(at = @At("HEAD"), method = "isClimbing", cancellable = true)
    public void doSpiderClimbing(CallbackInfoReturnable<Boolean> info) {
        if(PowerTypes.CLIMBING.isActive(this)) {
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
        return OriginComponent.modify(this, ModifySwimSpeedPower.class, in);
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
}
