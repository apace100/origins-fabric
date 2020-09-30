package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class InvisibilityPower extends Power {

    private final boolean renderArmor;

    public InvisibilityPower(PowerType<?> type, PlayerEntity player, boolean renderArmor) {
        super(type, player);
        this.renderArmor = renderArmor;
    }

    public boolean shouldRenderArmor() {
        return renderArmor;
    }
}
