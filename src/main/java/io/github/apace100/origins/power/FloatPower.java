package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class FloatPower extends Power {

    public final float value;

    public FloatPower(PowerType<?> type, PlayerEntity player, float value) {
        super(type, player);
        this.value = value;
    }
}
