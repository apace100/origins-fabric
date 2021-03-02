package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class ModifyJumpPower extends ValueModifyingPower {

    public ModifyJumpPower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }
}
