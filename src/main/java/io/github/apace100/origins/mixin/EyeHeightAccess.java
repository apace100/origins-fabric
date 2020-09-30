package io.github.apace100.origins.mixin;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface EyeHeightAccess {

    @Invoker
    float callGetEyeHeight(EntityPose pose, EntityDimensions dimensions);
}
