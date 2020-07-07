package io.github.apace100.origins.power;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ModifyDamageTakenPower extends Power {

    private final BiFunction<PlayerEntity, DamageSource, Boolean> condition;
    private final Function<Float, Float> modifier;

    public ModifyDamageTakenPower(PowerType<?> type, PlayerEntity player, BiFunction<PlayerEntity, DamageSource, Boolean> condition, Function<Float, Float> modifier) {
        super(type, player);
        this.condition = condition;
        this.modifier = modifier;
    }

    public boolean doesApply(DamageSource source) {
        return condition.apply(this.player, source);
    }

    public float apply(float damageAmount) {
        return modifier.apply(damageAmount);
    }
}
