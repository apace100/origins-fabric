package io.github.apace100.origins.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Invoker
    int callGetNextAirOnLand(int air);

    @Invoker
    int callGetNextAirUnderwater(int air);

}
