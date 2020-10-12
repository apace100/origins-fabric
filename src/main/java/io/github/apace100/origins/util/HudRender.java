package io.github.apace100.origins.util;

import net.minecraft.util.Identifier;

public class HudRender {

    private final boolean shouldRender;
    private final int barIndex;
    private final Identifier spriteLocation;

    public HudRender(boolean shouldRender, int barIndex, Identifier spriteLocation) {
        this.shouldRender = shouldRender;
        this.barIndex = barIndex;
        this.spriteLocation = spriteLocation;
    }

    public Identifier getSpriteLocation() {
        return spriteLocation;
    }

    public int getBarIndex() {
        return barIndex;
    }

    public boolean shouldRender() {
        return shouldRender;
    }
}
