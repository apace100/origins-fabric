package io.github.apace100.origins.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

@Environment(EnvType.CLIENT)
public interface TooltipDrawer {

	void renderTooltip(TextRenderer textRenderer, DrawContext context, int mouseX, int mouseY, float delta);

}
