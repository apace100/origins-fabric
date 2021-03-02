package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class ModifySwimSpeedPower extends ValueModifyingPower {
    public ModifySwimSpeedPower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }
}
