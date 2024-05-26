package io.github.apace100.origins.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(TargetPredicate.class)
public interface TargetPredicateAccessor {

    @Nullable
    @Accessor
    Predicate<LivingEntity> getPredicate();

}
