package io.github.apace100.origins.power;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

public class SimpleStatusEffectPower extends StatusEffectPower {
    public SimpleStatusEffectPower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }

    public SimpleStatusEffectPower(PowerType<?> type, PlayerEntity player, StatusEffectInstance effectInstance) {
        super(type, player, effectInstance);
    }
}
