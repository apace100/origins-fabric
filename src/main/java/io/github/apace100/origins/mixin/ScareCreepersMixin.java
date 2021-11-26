package io.github.apace100.origins.mixin;


import io.github.apace100.origins.power.OriginsPowerTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public abstract class ScareCreepersMixin extends HostileEntity {
    protected ScareCreepersMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "initGoals")
    private void addGoals(CallbackInfo info) {
        Goal goal = new FleeEntityGoal<>(this, PlayerEntity.class, OriginsPowerTypes.SCARE_CREEPERS::isActive, 6.0F, 1.0D, 1.2D, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test);
        this.goalSelector.add(3, goal);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 8), method = "initGoals")
    private void redirectTargetGoal(GoalSelector goalSelector, int priority, Goal goal) {
        Goal newGoal = new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, e -> !OriginsPowerTypes.SCARE_CREEPERS.isActive(e));
        goalSelector.add(priority, newGoal);
    }
}