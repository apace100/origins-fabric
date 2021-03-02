package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class ModifyExhaustionPower extends ValueModifyingPower {

    public ModifyExhaustionPower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }
}
