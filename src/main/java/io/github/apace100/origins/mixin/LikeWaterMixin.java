package io.github.apace100.origins.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.origins.power.type.LikeWaterPowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LikeWaterMixin extends Entity {

    private LikeWaterMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyReturnValue(method = "applyFluidMovingSpeed", at = @At(value = "RETURN", ordinal = 0))
    private Vec3d origins$modifyVerticalFluidMovingSpeed(Vec3d original) {
        return LikeWaterPowerType.modify(this, original);
    }

}
