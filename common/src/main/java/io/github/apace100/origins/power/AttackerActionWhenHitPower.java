package io.github.apace100.origins.power;

import io.github.apace100.origins.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AttackerActionWhenHitPower extends CooldownPower {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Consumer<Entity> entityAction;

    public AttackerActionWhenHitPower(PowerType<?> type, PlayerEntity player, int cooldownDuration, HudRender hudRender, Predicate<Pair<DamageSource, Float>> damageCondition, Consumer<Entity> entityAction) {
        super(type, player, cooldownDuration, hudRender);
        this.damageCondition = damageCondition;
        this.entityAction = entityAction;
    }

    public void whenHit(DamageSource damageSource, float damageAmount) {
        if(damageSource.getAttacker() != null && damageSource.getAttacker() != player) {
            if(damageCondition == null || damageCondition.test(new Pair<>(damageSource, damageAmount))) {
                if(canUse()) {
                    this.entityAction.accept(damageSource.getAttacker());
                    use();
                }
            }
        }
    }
}
