package io.github.apace100.origins.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyProjectileDamagePower extends ValueModifyingPower {

    private final Predicate<Pair<DamageSource, Float>> condition;
    private final Predicate<LivingEntity> targetCondition;

    private Consumer<LivingEntity> targetAction;
    private Consumer<LivingEntity> selfAction;

    public ModifyProjectileDamagePower(PowerType<?> type, PlayerEntity player, Predicate<Pair<DamageSource, Float>> condition, Predicate<LivingEntity> targetCondition) {
        super(type, player);
        this.condition = condition;
        this.targetCondition = targetCondition;
    }

    public boolean doesApply(DamageSource source, float damageAmount, LivingEntity target) {
        return condition.test(new Pair<>(source, damageAmount)) && (target == null || targetCondition == null || targetCondition.test(target));
    }

    public void setTargetAction(Consumer<LivingEntity> targetAction) {
        this.targetAction = targetAction;
    }

    public void setSelfAction(Consumer<LivingEntity> selfAction) {
        this.selfAction = selfAction;
    }

    public void executeActions(Entity target) {
        if(selfAction != null) {
            selfAction.accept(player);
        }
        if(targetAction != null && target instanceof LivingEntity) {
            targetAction.accept((LivingEntity)target);
        }
    }
}
