package io.github.apace100.origins.power;

import io.github.apace100.origins.util.HudRender;
import net.minecraft.entity.player.PlayerEntity;

public class HudRenderedVariableIntPower extends VariableIntPower implements HudRendered {

    private final HudRender hudRender;

    public HudRenderedVariableIntPower(PowerType<?> type, PlayerEntity player, HudRender hudRender, int startValue, int min, int max) {
        super(type, player, startValue, min, max);
        this.hudRender = hudRender;
    }

    @Override
    public HudRender getRenderSettings() {
        return hudRender;
    }

    @Override
    public float getFill() {
        return (this.getValue() - this.getMin()) / (float)(this.getMax() - this.getMin());
    }

    @Override
    public boolean shouldRender() {
        return true;
    }
}
