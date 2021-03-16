package io.github.apace100.origins.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class ModifyProjectileDamagePower extends ValueModifyingPower {

    private final Predicate<Pair<DamageSource, Float>> condition;
    private final Predicate<LivingEntity> targetCondition;

    public ModifyProjectileDamagePower(PowerType<?> type, PlayerEntity player, Predicate<Pair<DamageSource, Float>> condition, Predicate<LivingEntity> targetCondition) {
        super(type, player);
        this.condition = condition;
        this.targetCondition = targetCondition;
    }

    public boolean doesApply(DamageSource source, float damageAmount, LivingEntity target) {
        return condition.test(new Pair<>(source, damageAmount)) && (target == null || targetCondition == null || targetCondition.test(target));
    }
}
