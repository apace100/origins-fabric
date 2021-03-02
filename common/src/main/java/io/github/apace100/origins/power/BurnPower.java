package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class BurnPower extends Power {

    private final int refreshInterval;
    private final int burnDuration;

    public BurnPower(PowerType<?> type, PlayerEntity player, int refreshInterval, int burnDuration) {
        super(type, player);
        this.refreshInterval = refreshInterval;
        this.burnDuration = burnDuration;
        this.setTicking();
    }

    public void tick() {
        if(player.age % refreshInterval == 0) {
            player.setOnFireFor(burnDuration);
        }
    }
}
