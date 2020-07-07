package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Function;

public class NightVisionPower extends Power {

    private final Function<PlayerEntity, Float> strengthFunction;

    public NightVisionPower(PowerType<?> type, PlayerEntity player) {
        this(type, player, 1.0F);
    }

    public NightVisionPower(PowerType<?> type, PlayerEntity player, float strength) {
        this(type, player, pe -> strength);
    }

    public NightVisionPower(PowerType<?> type, PlayerEntity player, Function<PlayerEntity, Float> strengthFunction) {
        super(type, player);
        this.strengthFunction = strengthFunction;
    }

    public float getStrength() {
        return strengthFunction.apply(this.player);
    }
}
