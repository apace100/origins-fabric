package io.github.apace100.origins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.origins.mixin.ActiveTargetGoalAccessor;
import io.github.apace100.origins.mixin.MobEntityAccessor;
import io.github.apace100.origins.mixin.TargetPredicateAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.predicate.entity.EntityPredicates;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public class ScareCreepersPower extends Power {

    public ScareCreepersPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public static void modifyGoals(PathAwareEntity pathAwareEntity) {

        GoalSelector targetSelector = ((MobEntityAccessor) pathAwareEntity).getTargetSelector();
        GoalSelector goalSelector = ((MobEntityAccessor) pathAwareEntity).getGoalSelector();

        Iterator<PrioritizedGoal> oldTargetPrioGoals = targetSelector.getGoals().iterator();
        Set<PrioritizedGoal> newTargetPrioGoals = new HashSet<>();

        while (oldTargetPrioGoals.hasNext()) {

            PrioritizedGoal oldTargetPrioGoal = oldTargetPrioGoals.next();
            if (!(oldTargetPrioGoal.getGoal() instanceof ActiveTargetGoalAccessor oldTargetGoal)) {
                continue;
            }

            Predicate<LivingEntity> targetCondition = MiscUtil.combineAnd(((TargetPredicateAccessor) oldTargetGoal.getTargetPredicate()).getPredicate(), e -> !PowerHolderComponent.hasPower(e, ScareCreepersPower.class));
            PrioritizedGoal newTargetPrioGoal = new PrioritizedGoal(oldTargetPrioGoal.getPriority(), new ActiveTargetGoal<>(pathAwareEntity, oldTargetGoal.getTargetClass(), oldTargetGoal.getReciprocalChance(), oldTargetGoal.getCheckVisibility(), oldTargetGoal.getCheckCanNavigate(), targetCondition));

            newTargetPrioGoals.add(newTargetPrioGoal);
            oldTargetPrioGoals.remove();

        }

        goalSelector.add(3, new FleeEntityGoal<>(pathAwareEntity, LivingEntity.class, e -> PowerHolderComponent.hasPower(e, ScareCreepersPower.class), 6.0F, 1.0D, 1.2D, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test));
        newTargetPrioGoals.forEach(pg -> targetSelector.add(pg.getPriority(), pg.getGoal()));

    }

}
