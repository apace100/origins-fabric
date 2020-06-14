package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class InvulnerablePower extends Power {

    public InvulnerablePower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }

    @Override
    public void onAdded() {
        player.setInvulnerable(true);
    }

    @Override
    public void onRemoved() {
        player.setInvulnerable(player.isCreative() || player.isSpectator());
    }
}
