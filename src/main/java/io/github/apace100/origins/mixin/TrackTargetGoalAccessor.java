package io.github.apace100.origins.mixin;

import net.minecraft.entity.ai.goal.TrackTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrackTargetGoal.class)
public interface TrackTargetGoalAccessor {

    @Accessor
    boolean getCheckVisibility();

    @Accessor
    boolean getCheckCanNavigate();

}
