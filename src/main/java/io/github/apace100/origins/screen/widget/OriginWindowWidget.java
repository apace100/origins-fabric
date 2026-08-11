package io.github.apace100.origins.screen.widget;

import io.github.apace100.apoli.power.MultiplePower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.TextAlignment;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginManager;
import io.github.apace100.origins.screen.TooltipDrawer;
import io.github.apace100.origins.util.MiscUtilClient;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class OriginWindowWidget extends ClickableWidget {

	private static final int LINE_HEIGHT = 11;

	private final TextRenderer textRenderer;
	private final List<TooltipDrawer> hoveredTooltips;

	private Origin.WindowTextures textures = Origin.WindowTextures.DEFAULT;
	private MutableText randomDescription = Text.empty();

	@Nullable
	private Origin origin;
	@Nullable
	private OriginLayer layer;

	private boolean random;
	private boolean dragScrolling;

	private int scrollY;
	private int contentsHeight;

	public OriginWindowWidget(int x, int y, int width, int height, TextRenderer textRenderer) {
		super(x, y, width, height, Text.empty());
		this.textRenderer = textRenderer;
		this.hoveredTooltips = new ObjectArrayList<>();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {

		if (!this.active || !this.visible) {
			return false;
		}

		boolean clicked = this.isMouseOver(mouseX, mouseY);
		this.dragScrolling = this.overflowing()
			&& this.isValidClickButton(button)
			&& mouseX >= this.getX() + this.getWidth()
			&& mouseX <= this.getX() + this.getWidth() + this.getScrollerWidth()
			&& mouseY >= this.getY()
			&& mouseY <= this.getY() + this.getHeight();

		return clicked
			|| dragScrolling;

	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {

		if (!this.visible || !this.isFocused() || !this.dragScrolling) {
			return false;
		}

		if (mouseY < this.getY()) {
			this.setScrollY(0);
		}

		else if (mouseY > this.getY() + this.getHeight()) {
			this.setScrollY(this.getMaxScrollY());
		}

		else {

			int thumbHeight = this.getScrollBarThumbHeight();
			int scrolled = Math.max(1, this.getMaxScrollY() / (this.getHeight() - thumbHeight));

			this.setScrollY(this.getScrollY() + (int) deltaY * scrolled);

		}

		return true;

	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {

		if (this.visible) {
			this.setScrollY(this.scrollY - (int) verticalAmount * this.getDeltaYPerScroll());
		}

		return this.visible;

	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		this.dragScrolling = false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

		boolean pressedUpArrow = keyCode == GLFW.GLFW_KEY_UP;
		boolean pressedDownArrow = keyCode == GLFW.GLFW_KEY_DOWN;

		if (this.isFocused() && (pressedUpArrow || pressedDownArrow)) {

			int prevScrollY = this.getScrollY();
			this.setScrollY(this.scrollY + (pressedUpArrow ? -1 : 1) * this.getDeltaYPerScroll());

			if (prevScrollY != this.getScrollY()) {
				return true;
			}

		}

		return super.keyPressed(keyCode, scanCode, modifiers);

	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {

		if (!this.visible) {
			return;
		}

		this.contentsHeight = 0;
		this.hoveredTooltips.clear();

		//  Draw the window's background
		context.drawGuiTexture(textures.background(), this.getX(), this.getY(), this.getWidth(), this.getHeight());

		//  Draw the contents of the window
		context.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() + 1, this.getY() + this.getHeight() - 1);
		context.getMatrices().push();
		context.getMatrices().translate(0, 0, 1);

		this.renderPowersAndBadges(context, mouseX, mouseY, delta);
		this.renderNameAndImpact(context, mouseX, mouseY, delta);

		context.getMatrices().pop();
		context.disableScissor();

		//  Draw the scrollbar at the side of the window
		if (this.overflowing()) {
			this.renderScroller(context);
		}

		//  Draw the border and the tooltips of the hovered elements
		context.drawGuiTexture(textures.border(), this.getX(), this.getY(), 200, this.getWidth(), this.getHeight());
		this.hoveredTooltips.forEach(tooltip -> tooltip.renderTooltip(textRenderer, context, mouseX, mouseY, delta));

	}

	public void show(@Nullable Origin origin, @Nullable OriginLayer layer, Function<Origin, Origin.WindowTextures> texturesGetter) {

		this.origin = origin;
		this.layer = layer;

		this.random = origin == Origin.RANDOM;

		this.randomDescription = Text.empty();
		this.scrollY = 0;

		if (layer == null || origin == null) {
			return;
		}

		ObjectSortedSet<Origin> randoms = new ObjectAVLTreeSet<>(Origin::compareTo);
		layer.getRandomOrigins(MinecraftClient.getInstance().player).forEach(id -> randoms.add(OriginManager.get(id)));

		for (var random : randoms) {
			this.randomDescription.append(Text.of("\n")).append("- ").append(random.getName());
		}

		this.textures = texturesGetter.apply(origin);

	}

	protected void renderNameAndImpact(DrawContext context, int mouseX, int mouseY, float delta) {

		if (origin == null) {
			return;
		}

		int x = this.getX();
		int y = this.getY() - this.getScrollY();

		ItemStack icon = origin.getDisplayItem();
		ImpactWidget impactWidget = new ImpactWidget(origin.getImpact(), (x + this.getWidth()) - 48, y + 19);

		int nameStartX = x + 40;
		int nameStartY = y + 18;
		int nameEndX = impactWidget.getX() - 2;
		int nameEndY = nameStartY + 9;

		context.drawGuiTexture(textures.namePlate(), x + 10, y + 10, this.getWidth() - 26, 26);
		MiscUtilClient.drawScrollingText(context, textRenderer, origin.getName(), TextAlignment.LEFT, nameStartX, nameStartY, nameEndX, nameEndY, 0xFFFFFF, true);

		impactWidget.renderWidget(context, mouseX, mouseY, delta);
		context.drawItem(icon, x + 15, y + 15);

		if (impactWidget.isMouseOver(mouseX, mouseY) && context.scissorContains(mouseX, mouseY)) {
			hoveredTooltips.add(impactWidget);
		}

	}

	protected void renderPowersAndBadges(DrawContext context, int mouseX, int mouseY, float delta) {

		if (origin == null || layer == null) {
			return;
		}

		int x = this.getX() + 18;
		int y = (this.getY() + 45) - this.getScrollY();

		int widthLimit = this.getWidth() - 30;
		this.contentsHeight = this.getY() + 45;

		Text description = origin == Origin.EMPTY && layer.getMissingDescription() != null
			? layer.getMissingDescription()
			: origin.getDescription();

		for (var line : textRenderer.wrapLines(description, widthLimit)) {

			context.drawTextWithShadow(textRenderer, line, x, y, 0xCCCCCC);

			y += LINE_HEIGHT;
			contentsHeight += LINE_HEIGHT;

		}

		if (random) {

			for (var line : textRenderer.wrapLines(randomDescription, widthLimit)) {

				context.drawTextWithShadow(textRenderer, line, x, y, 0xCCCCCC);

				y += LINE_HEIGHT;
				contentsHeight += LINE_HEIGHT;

			}

		}

		else {

			for (var power : origin.getPowers()) {

				if (power.isHidden()) {
					continue;
				}

				y += LINE_HEIGHT;
				contentsHeight += LINE_HEIGHT;

				List<OrderedText> powerName = new ObjectArrayList<>(textRenderer.wrapLines(power.getName().formatted(Formatting.UNDERLINE), widthLimit));
				int powerNameWidth = textRenderer.getWidth(powerName.getLast());

				for (var powerNameLine : powerName) {

					context.drawTextWithShadow(textRenderer, powerNameLine, x, y, 0xFFFFFF);

					y += LINE_HEIGHT;
					contentsHeight += LINE_HEIGHT;

				}

				y -= LINE_HEIGHT;
				contentsHeight -= LINE_HEIGHT;

				int badgeStartX = x + powerNameWidth + 4;
				int badgeEndX = x + (this.getWidth() - 6);

				int badgeOffsetX = 0;
				int badgeOffsetY = 0;

				for (var badgeWidget : this.getBadgeWidgets(power)) {

					int badgeX = badgeStartX + (badgeOffsetX * 10);
					int badgeY = (y - 1) + (badgeOffsetY * 10);

					if (badgeX >= badgeEndX) {

						badgeOffsetX = 0;
						badgeOffsetY++;

						badgeX = badgeStartX = x;
						badgeY = (y - 1) + (badgeOffsetY * 10);

					}

					badgeWidget.setPosition(badgeX, badgeY);
					badgeWidget.render(context, mouseX, mouseY, delta);

					badgeOffsetX++;

					if (badgeWidget.hasTooltip() && badgeWidget.isMouseOver(mouseX, mouseY) && context.scissorContains(mouseX, mouseY)) {
						hoveredTooltips.add(badgeWidget);
					}

				}

				int badgeOffset = Math.max(badgeOffsetY, 1) * 10;

				y += badgeOffset;
				contentsHeight += badgeOffset;

				for (var powerDescriptionLine : textRenderer.wrapLines(power.getDescription(), widthLimit)) {

					context.drawTextWithShadow(textRenderer, powerDescriptionLine, x, y, 0xCCCCCC);

					y += LINE_HEIGHT;
					contentsHeight += LINE_HEIGHT;

				}

			}

		}

	}

	protected Collection<BadgeWidget> getBadgeWidgets(Power power) {

		if (!BadgeManager.hasPowerBadges(power)) {

			if (power instanceof MultiplePower multiplePower) {

				List<BadgeWidget> widgets = new ObjectArrayList<>();
				for (var subPower : multiplePower.getSubPowers()) {

					for (var badge : BadgeManager.getPowerBadges(subPower.getId())) {
						widgets.add(new BadgeWidget(subPower, badge, 0, 0));
					}

				}

				return widgets;

			}

			else {
				return List.of();
			}

		}

		else {

			List<BadgeWidget> widgets = new ObjectArrayList<>();
			for (var badge : BadgeManager.getPowerBadges(power.getId())) {
				widgets.add(new BadgeWidget(power, badge, 0, 0));
			}

			return widgets;

		}

	}

	protected boolean overflowing() {
		return this.getContentsHeight() > this.getHeight()
			&& this.getMaxScrollY() > 0;
	}

	protected int getContentsHeightWithPadding() {
		return this.getContentsHeight() + this.getContentsPadding();
	}

	protected int getContentsPadding() {
		return 8;
	}

	protected int getScrollBarThumbHeight() {
		return MathHelper.clamp((int) ((float) (this.getHeight() * this.getHeight()) / (float) (this.getContentsHeightWithPadding())), 27, this.getHeight());
	}

	protected int getScrollerWidth() {
		return 8;
	}

	protected int getDeltaYPerScroll() {
		return Screen.hasShiftDown() ? 4 : 12;
	}

	protected int getContentsHeight() {
		return contentsHeight;
	}

	protected int getMaxScrollY() {
		return Math.max(0, (this.getContentsHeightWithPadding() - (this.height - this.getContentsPadding())) - this.getY());
	}

	protected int getScrollY() {
		return scrollY;
	}

	protected void setScrollY(int scrollY) {
		this.scrollY = MathHelper.clamp(scrollY, 0, this.getMaxScrollY());
	}

	protected void renderScroller(DrawContext context) {

		int width = this.getScrollerWidth();
		int height = this.getScrollBarThumbHeight();
		int x = this.getX() + this.getWidth();
		int y = Math.max(this.getY(), ((this.getScrollY() * (this.getHeight() - height)) / this.getMaxScrollY()) + this.getY());

		context.drawGuiTexture(textures.scroller().slot(), x, this.getY(), width, this.getHeight());
		context.drawGuiTexture(dragScrolling ? textures.scroller().pressed() : textures.scroller().unpressed(),  x, y, width, height);

	}

}
