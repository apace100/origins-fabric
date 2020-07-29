package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class HudRenderedVariableIntPower extends VariableIntPower implements HudRendered {

    private final int barIndex;

    public HudRenderedVariableIntPower(PowerType<?> type, PlayerEntity player, int barIndex, int startValue, int min, int max) {
        super(type, player, startValue, min, max);
        this.barIndex = barIndex;
    }

    @Override
    public int getBarIndex() {
        return barIndex;
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
