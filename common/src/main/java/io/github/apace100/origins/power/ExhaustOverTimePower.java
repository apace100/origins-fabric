package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class ExhaustOverTimePower extends Power {

    private final int exhaustInterval;
    private final float exhaustion;

    public ExhaustOverTimePower(PowerType<?> type, PlayerEntity player, int exhaustInterval, float exhaustion) {
        super(type, player);
        this.exhaustInterval = exhaustInterval;
        this.exhaustion = exhaustion;
        this.setTicking();
    }

    public void tick() {
        if(player.age % exhaustInterval == 0) {
            player.addExhaustion(exhaustion);
        }
    }
}
