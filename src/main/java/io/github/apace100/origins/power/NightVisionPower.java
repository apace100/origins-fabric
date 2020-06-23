package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Function;
import java.util.function.Predicate;

public class NightVisionPower extends Power {

    private final Predicate<PlayerEntity> condition;
    private final Function<PlayerEntity, Float> strengthFunction;

    public NightVisionPower(PowerType<?> type, PlayerEntity player, Predicate<PlayerEntity> condition) {
        this(type, player, condition, 1.0F);
    }

    public NightVisionPower(PowerType<?> type, PlayerEntity player, Predicate<PlayerEntity> condition, float strength) {
        this(type, player, condition, pe -> strength);
    }

    public NightVisionPower(PowerType<?> type, PlayerEntity player, Predicate<PlayerEntity> condition, Function<PlayerEntity, Float> strengthFunction) {
        super(type, player);
        this.condition = condition;
        this.strengthFunction = strengthFunction;
    }

    public boolean isActive() {
        return condition.test(this.player);
    }

    public float getStrength() {
        return strengthFunction.apply(this.player);
    }
}
