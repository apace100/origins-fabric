package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class ClimbingPower extends Power {

    public final boolean allowHolding;

    public ClimbingPower(PowerType<?> type, PlayerEntity player, boolean allowHolding) {
        super(type, player);
        this.allowHolding = allowHolding;
    }
}
