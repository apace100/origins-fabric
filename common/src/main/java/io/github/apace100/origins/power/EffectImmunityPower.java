package io.github.apace100.origins.power;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;

public class EffectImmunityPower extends Power {

    protected final HashSet<StatusEffect> effects = new HashSet<>();

    public EffectImmunityPower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }
    public EffectImmunityPower(PowerType<?> type, PlayerEntity player, StatusEffect effect) {
        super(type, player);
        addEffect(effect);
    }

    public EffectImmunityPower addEffect(StatusEffect effect) {
        effects.add(effect);
        return this;
    }

    public boolean doesApply(StatusEffectInstance instance) {
        return doesApply(instance.getEffectType());
    }

    public boolean doesApply(StatusEffect effect) {
        return effects.contains(effect);
    }
}
