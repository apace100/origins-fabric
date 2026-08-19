package io.github.apace100.origins.screen.widget;

import io.github.apace100.apoli.power.MultiplePower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.TextAlignment;
import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginManager;
import io.github.apace100.origins.screen.DrawableTooltip;
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
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

//  TODO:   Optimize the rendering impl. by caching the sub-elements (like lines of the origin's name/description,
//          power names/descriptions, etc.)
@Environment(EnvType.CLIENT)
public class OriginWindowWidget extends ScrollableWidget {

	public static final int LINE_HEIGHT = 11;

	public static final int WINDOW_WIDTH = 176;
	public static final int WINDOW_HEIGHT = 182;

	public static final int NAMEPLATE_WIDTH = 150;
	public static final int NAMEPLATE_HEIGHT = 26;

	private final List<DrawableTooltip> tooltips = new ObjectArrayList<>();
	private int lines;

	private OverlayContext overlayContext = OverlayContext.EMPTY;
	@Nullable
	private TextRenderer textRenderer;

	private Origin.WindowTextures textures = Origin.WindowTextures.DEFAULT;
	@Nullable
	private MutableText randomOrigins;

	@Nullable
	private Origin origin;
	@Nullable
	private OriginLayer layer;

	public OriginWindowWidget() {
		super(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, Text.empty());
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {

		this.overlayContext = new OverlayContext(mouseX, mouseY, delta);
		this.lines = 0;

		super.renderWidget(context, mouseX, mouseY + Math.round((float) this.getScrollY()), delta);

	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	@Override
	protected int getContentsHeight() {
		return lines * LINE_HEIGHT;
	}

	@Override
	protected double getDeltaYPerScroll() {
		return Screen.hasShiftDown() ? LINE_HEIGHT / 2.0 : LINE_HEIGHT;
	}

	@Override
	protected void drawBox(DrawContext context) {
		context.drawGuiTexture(textures.background(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderPowersAndBadges(context, mouseX, mouseY, delta);
		this.renderNameAndImpact(context, mouseX, mouseY, delta);
	}

	@Override
	protected void renderOverlay(DrawContext context) {

		super.renderOverlay(context);
		context.drawGuiTexture(textures.border(), this.getX(), this.getY(), 200, this.getWidth(), this.getHeight());

		if (textRenderer == null) {
			return;
		}

		Iterator<DrawableTooltip> tooltipIterator = tooltips.iterator();
		while (tooltipIterator.hasNext()) {

			var tooltip = tooltipIterator.next();
			tooltip.renderTooltip(textRenderer, context, overlayContext.mouseX(), overlayContext.mouseY(), overlayContext.delta());

			tooltipIterator.remove();

		}

	}

	public int getTextMaxWidth() {
		return this.getWidth() - 30;
	}

	public int getCenterX() {
		return this.getX() + (this.getWidth() / 2);
	}

	public int getCenterY() {
		return this.getY() + (this.getHeight() / 2);
	}

	public void init(TextRenderer renderer) {
		this.textRenderer = renderer;
	}

	public void show(@NotNull Origin origin, @NotNull OriginLayer layer, Function<Origin, Origin.@NotNull WindowTextures> texturesGetter) {

		this.origin = origin;
		this.layer = layer;

		if (origin == Origin.RANDOM) {

			this.randomOrigins = Text.empty();

			ObjectSortedSet<Origin> randoms = new ObjectAVLTreeSet<>(Origin::compareTo);
			layer.getRandomOrigins(MinecraftClient.getInstance().player).forEach(id -> randoms.add(OriginManager.get(id)));

			for (var random : randoms) {
				this.randomOrigins.append(Text.of("\n")).append("- ").append(random.getName());
			}

		}

		else {
			this.randomOrigins = null;
		}

		this.textures = texturesGetter.apply(origin);

	}

	public void resetAndShow(@NotNull Origin origin, @NotNull OriginLayer layer, Function<Origin, Origin.@NotNull WindowTextures> texturesGetter) {
		this.show(origin, layer, texturesGetter);
		this.setScrollY(0.0);
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

	protected void renderPowersAndBadges(DrawContext context, int mouseX, int mouseY, float delta) {

		if (textRenderer == null) {
			return;
		}

		int x = this.getX() + 18;
		int y = this.getY() + 45;

		if (origin == null || layer == null) {

			Text emptyOrNotInstalledText = OriginsClient.isServerRunningOrigins
				? Text.translatable("origins.gui.view_origin.empty")
				: Text.translatable("origins.gui.view_origin.not_installed");

			for (var line : textRenderer.wrapLines(emptyOrNotInstalledText, this.getTextMaxWidth())) {
				context.drawCenteredTextWithShadow(textRenderer, line, this.getCenterX(), y, -1);
				y += LINE_HEIGHT;
			}

		}

		else {

			Text description = origin == Origin.EMPTY && layer.getMissingDescription() != null
				? layer.getMissingDescription()
				: origin.getDescription();

			for (var descriptionLine : textRenderer.wrapLines(description, this.getTextMaxWidth())) {

				context.drawTextWithShadow(textRenderer, descriptionLine, x, y, 0xCCCCCC);

				y += LINE_HEIGHT;
				lines++;

			}

			if (randomOrigins != null) {

				for (var randomOriginLine : textRenderer.wrapLines(randomOrigins, this.getTextMaxWidth())) {

					context.drawTextWithShadow(textRenderer, randomOriginLine, x, y, 0xCCCCCC);

					y += LINE_HEIGHT;
					lines++;

				}

			}

			else {

				for (var power : origin.getPowers()) {

					if (power.isHidden()) {
						continue;
					}

					y += LINE_HEIGHT;
					lines++;

					List<OrderedText> nameLines = new ObjectArrayList<>(textRenderer.wrapLines(power.getName().formatted(Formatting.UNDERLINE), this.getTextMaxWidth()));
					int nameWidth = textRenderer.getWidth(nameLines.getLast());

					for (var nameLine : nameLines) {

						context.drawTextWithShadow(textRenderer, nameLine, x, y, 0xFFFFFF);

						y += LINE_HEIGHT;
						lines++;

					}

					y -= LINE_HEIGHT;
					lines--;

					int badgeStartX = x + nameWidth + 4;
					int badgeEndX = x + (this.getWidth() - 6);

					int badgeOffsetX = 0;
					int badgeOffsetY = 0;

					for (var badgeWidget : this.getBadgeWidgets(power)) {

						int badgeX = badgeStartX + (badgeOffsetX * Badge.SIZE);
						int badgeY = (y - 1) + (badgeOffsetY * Badge.SIZE);

						if (badgeX >= badgeEndX) {

							badgeOffsetX = 0;
							badgeOffsetY++;

							badgeX = badgeStartX = x;
							badgeY = (y - 1) + (badgeOffsetY * Badge.SIZE);

						}

						badgeWidget.setPosition(badgeX, badgeY);
						badgeWidget.render(context, mouseX, mouseY, delta);

						badgeOffsetX++;

						if (badgeWidget.hasTooltip() && badgeWidget.isHovered()) {
							tooltips.add(badgeWidget);
						}

					}

					int badgeOffset = Math.max(badgeOffsetY, 1);

					y += badgeOffset * Badge.SIZE;
					lines += badgeOffset;

					for (var powerDescriptionLine : textRenderer.wrapLines(power.getDescription(), this.getTextMaxWidth())) {

						context.drawTextWithShadow(textRenderer, powerDescriptionLine, x, y, 0xCCCCCC);

						y += LINE_HEIGHT;
						lines++;

					}

				}

			}

		}

		lines += 5;

	}

	protected void renderNameAndImpact(DrawContext context, int mouseX, int mouseY, float delta) {

		if (origin == null) {
			return;
		}

		int y = this.getY();

		int namePlateXStart = this.getCenterX() - (NAMEPLATE_WIDTH / 2);
		int namePlateXEnd = namePlateXStart + NAMEPLATE_WIDTH;

		context.drawGuiTexture(textures.namePlate(), namePlateXStart, y + 10, NAMEPLATE_WIDTH, NAMEPLATE_HEIGHT);

		ItemStack icon = origin.getDisplayItem();
		ImpactWidget impactWidget = new ImpactWidget(origin.getImpact(), namePlateXEnd - 32, y + 19);

		int nameStartX = namePlateXStart + 30;
		int nameStartY = y + 18;
		int nameEndX = impactWidget.getX() - 2;
		int nameEndY = nameStartY + 9;

		if (textRenderer != null) {
			MiscUtilClient.drawScrollingText(context, textRenderer, origin.getName(), TextAlignment.CENTER, nameStartX, nameStartY, nameEndX, nameEndY, 0xFFFFFF, true);
		}

		impactWidget.render(context, mouseX, mouseY, delta);
		context.drawItem(icon, namePlateXStart + 5, y + 15);

		if (impactWidget.isHovered()) {
			this.tooltips.add(impactWidget);
		}

	}

	record OverlayContext(int mouseX, int mouseY, float delta) {
		public static final OverlayContext EMPTY = new OverlayContext(0, 0, 0.0F);
	}

}
