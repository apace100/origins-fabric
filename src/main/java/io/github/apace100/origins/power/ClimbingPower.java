package io.github.apace100.origins.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

public class ClimbingPower extends Power {

    private final boolean allowHolding;
    private final Predicate<LivingEntity> holdingCondition;

    public ClimbingPower(PowerType<?> type, PlayerEntity player, boolean allowHolding, Predicate<LivingEntity> holdingCondition) {
        super(type, player);
        this.allowHolding = allowHolding;
        this.holdingCondition = holdingCondition;
    }

    public boolean canHold() {
        return allowHolding && (holdingCondition == null ? isActive() : holdingCondition.test(player));
    }
}
