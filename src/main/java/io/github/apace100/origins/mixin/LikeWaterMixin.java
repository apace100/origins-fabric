package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.OriginsPowerTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LikeWaterMixin extends Entity {

    public LikeWaterMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyFluidMovingSpeed(DZLnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d method_26317Proxy(LivingEntity entity, double d, boolean bl, Vec3d vec3d) {
        Vec3d oldReturn = entity.applyFluidMovingSpeed(d, bl, vec3d);
        if(OriginsPowerTypes.LIKE_WATER.isActive(this)) {
            if (Math.abs(vec3d.y - d / 16.0D) < 0.025D) {
                return new Vec3d(oldReturn.x, 0, oldReturn.z);
            }
        }
        return entity.applyFluidMovingSpeed(d, bl, vec3d);
    }
}
