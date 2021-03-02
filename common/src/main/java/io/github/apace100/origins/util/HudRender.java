package io.github.apace100.origins.util;

import io.github.apace100.origins.Origins;
import net.minecraft.util.Identifier;

public class HudRender {

    public static final HudRender DONT_RENDER = new HudRender(false, 0, Origins.identifier("textures/gui/resource_bar.png"));

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
