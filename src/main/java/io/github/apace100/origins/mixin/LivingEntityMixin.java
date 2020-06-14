package io.github.apace100.origins.mixin;

import io.github.apace100.origins.block.TemporaryCobwebBlock;
import io.github.apace100.origins.power.CooldownPower;
import io.github.apace100.origins.power.PowerTypes;
import io.github.apace100.origins.registry.ModBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
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

    // WEBBING
    @Inject(at = @At("HEAD"), method = "onAttacking")
    public void onAttacking(Entity target, CallbackInfo info) {
        if(target instanceof LivingEntity) {
            if(PowerTypes.WEBBING.isActive(this)) {
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
