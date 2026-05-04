package io.github.apace100.origins.screen.widget;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.screen.TooltipDrawer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public final class ImpactWidget extends ClickableWidget implements TooltipDrawer {

	private final Impact impact;

	public ImpactWidget(Impact impact, int x, int y) {
		super(x, y, 28, 8, Text.translatable(Origins.MODID + ".gui.impact.impact").append(": ").append(impact.getTextComponent()));
		this.impact = impact;
	}

	@Override
	public void renderTooltip(TextRenderer textRenderer, DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawTooltip(textRenderer, this.getMessage(), mouseX, mouseY);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawGuiTexture(impact.getSpriteId(), this.getX(), this.getY(), 28, 8);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

}
