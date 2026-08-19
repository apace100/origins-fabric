package io.github.apace100.origins.screen.widget;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.mixin.DrawContextAccessor;
import io.github.apace100.origins.screen.DrawableTooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class BadgeWidget extends ClickableWidget implements DrawableTooltip {

	private final Power power;
	private final Badge badge;

	public BadgeWidget(Power power, Badge badge, int x, int y) {
		super(x, y, Badge.SIZE, Badge.SIZE, Text.empty());
		this.power = power;
		this.badge = badge;
	}

	@Override
	public void setWidth(int width) {
		//  No-op; badges aren't supposed to be resizable
	}

	@Override
	public void setHeight(int height) {
		//  No-op; badges aren't supposed to be resizable
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawTexture(badge.spriteId(), this.getX(), this.getY(), 0, 0, 9, 9, 9, 9);
	}

	@Override
	public void renderTooltip(TextRenderer textRenderer, DrawContext context, int mouseX, int mouseY, float delta) {

		MinecraftClient client = MinecraftClient.getInstance();
		Screen currentScreen = client.currentScreen;

		if (currentScreen != null) {
			((DrawContextAccessor) context).invokeDrawTooltip(textRenderer, this.getTooltipComponents(textRenderer, currentScreen.width - mouseX - 24, delta), mouseX, mouseY, HoveredTooltipPositioner.INSTANCE);
		}

	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	public List<TooltipComponent> getTooltipComponents(TextRenderer textRenderer, int widthLimit, float delta) {
		return badge.getTooltipComponents(power, widthLimit, delta, textRenderer);
	}

	public boolean hasTooltip() {
		return badge.hasTooltip();
	}

}
