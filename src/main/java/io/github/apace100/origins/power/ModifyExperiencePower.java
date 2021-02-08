package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class ModifyExperiencePower extends ValueModifyingPower {

    public ModifyExperiencePower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }
}
