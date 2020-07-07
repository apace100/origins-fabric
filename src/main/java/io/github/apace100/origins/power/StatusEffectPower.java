package io.github.apace100.origins.power;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

import java.util.LinkedList;
import java.util.List;

public class StatusEffectPower extends Power {

    protected final List<StatusEffectInstance> effects = new LinkedList<>();

    public StatusEffectPower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }
    public StatusEffectPower(PowerType<?> type, PlayerEntity player, StatusEffectInstance effectInstance) {
        super(type, player);
        addEffect(effectInstance);
    }

    public StatusEffectPower addEffect(StatusEffect effect) {
        return addEffect(effect, 80);
    }

    public StatusEffectPower addEffect(StatusEffect effect, int lingerDuration) {
        return addEffect(effect, lingerDuration, 0);
    }

    public StatusEffectPower addEffect(StatusEffect effect, int lingerDuration, int amplifier) {
        return addEffect(new StatusEffectInstance(effect, lingerDuration, amplifier));
    }

    public StatusEffectPower addEffect(StatusEffectInstance instance) {
        effects.add(instance);
        return this;
    }

    public void applyEffects() {
        effects.stream().map(StatusEffectInstance::new).forEach(player::addStatusEffect);
    }
}
