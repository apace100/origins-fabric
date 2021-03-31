package io.github.apace100.origins.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyDamageTakenPower extends ValueModifyingPower {

    private final Predicate<Pair<DamageSource, Float>> condition;

    private Consumer<LivingEntity> attackerAction;
    private Consumer<LivingEntity> selfAction;

    public ModifyDamageTakenPower(PowerType<?> type, PlayerEntity player, Predicate<Pair<DamageSource, Float>> condition) {
        super(type, player);
        this.condition = condition;
    }

    public boolean doesApply(DamageSource source, float damageAmount) {
        return condition.test(new Pair(source, damageAmount));
    }

    public void setAttackerAction(Consumer<LivingEntity> attackerAction) {
        this.attackerAction = attackerAction;
    }

    public void setSelfAction(Consumer<LivingEntity> selfAction) {
        this.selfAction = selfAction;
    }

    public void executeActions(Entity attacker) {
        if(selfAction != null) {
            selfAction.accept(player);
        }
        if(attackerAction != null && attacker instanceof LivingEntity) {
            attackerAction.accept((LivingEntity)attacker);
        }
    }
}
