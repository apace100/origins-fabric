package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class ElytraFlightPower extends Power {

    private final boolean renderElytra;

    public ElytraFlightPower(PowerType<?> type, PlayerEntity player, boolean renderElytra) {
        super(type, player);
        this.renderElytra = renderElytra;
    }

    public boolean shouldRenderElytra() {
        return renderElytra;
    }
}
