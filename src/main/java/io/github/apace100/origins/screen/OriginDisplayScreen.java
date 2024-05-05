package io.github.apace100.origins.screen;

import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.screen.widget.ScrollingTextWidget;
import io.github.apace100.apoli.util.TextAlignment;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.mixin.DrawContextAccessor;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class OriginDisplayScreen extends Screen {

    @SuppressWarnings("unused") //  The old sprite sheet for the origin screen
    private static final Identifier WINDOW = new Identifier(Origins.MODID, "textures/gui/choose_origin.png");

    private static final Identifier WINDOW_BACKGROUND = Origins.identifier("choose_origin/background");
    private static final Identifier WINDOW_BORDER = Origins.identifier("choose_origin/border");
    private static final Identifier WINDOW_NAME_PLATE = Origins.identifier("choose_origin/name_plate");
    private static final Identifier WINDOW_SCROLL_BAR = Origins.identifier("choose_origin/scroll_bar");
    private static final Identifier WINDOW_SCROLL_BAR_PRESSED = Origins.identifier("choose_origin/scroll_bar/pressed");
    private static final Identifier WINDOW_SCROLL_BAR_SLOT = Origins.identifier("choose_origin/scroll_bar/slot");

    protected static final int WINDOW_WIDTH = 176;
    protected static final int WINDOW_HEIGHT = 182;

    private final LinkedList<RenderedBadge> renderedBadges = new LinkedList<>();

    protected final boolean showDirtBackground;

    private Origin origin;
    private Origin prevOrigin;
    private OriginLayer layer;
    private OriginLayer prevLayer;
    private Text randomOriginText;
    private ScrollingTextWidget originNameWidget;

    private boolean refreshOriginNameWidget = false;

    private boolean isOriginRandom;
    private boolean dragScrolling = false;

    private double mouseDragStart = 0;
    private float time = 0;

    private int currentMaxScroll = 0;
    private int scrollDragStart = 0;

    protected int guiTop, guiLeft;
    protected int scrollPos = 0;


    public OriginDisplayScreen(Text title, boolean showDirtBackground) {
        super(title);
        this.showDirtBackground = showDirtBackground;
    }

    public void showOrigin(Origin origin,OriginLayer layer, boolean isRandom) {
        this.origin = origin;
        this.layer = layer;
        this.isOriginRandom = isRandom;
        this.scrollPos = 0;
        this.time = 0;
    }

    public void setRandomOriginText(Text text) {
        this.randomOriginText = text;
    }

    @Override
    protected void init() {

        super.init();

        guiLeft = (this.width - WINDOW_WIDTH) / 2;
        guiTop = (this.height - WINDOW_HEIGHT) / 2;

        originNameWidget = new ScrollingTextWidget(guiLeft + 38, guiTop + 18, WINDOW_WIDTH - (62 + 3 * 8), 9, Text.empty(), true, textRenderer);
        refreshOriginNameWidget = true;

    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {

        if (!showDirtBackground) {
            this.renderInGameBackground(context);
        } else {
            this.renderBackgroundTexture(context);
        }

        context.drawGuiTexture(WINDOW_BACKGROUND, guiLeft, guiTop, -4, WINDOW_WIDTH, WINDOW_HEIGHT);

    }

    @Override
    public void renderInGameBackground(DrawContext context) {
        context.fillGradient(0, 0, this.width, this.height, -5, 1678774288, -2112876528);
    }

    @Override
    public void renderBackgroundTexture(DrawContext context) {
        context.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
        context.drawTexture(OPTIONS_BACKGROUND_TEXTURE, 0, 0, -5, 0.0F, 0.0F, this.width, this.height, 32, 32);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        renderedBadges.clear();
        this.time += delta;

        this.renderBackground(context, mouseX, mouseY, delta);
        this.renderOriginWindow(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        if(origin != null) {
            renderScrollbar(context, mouseX, mouseY);
            renderBadgeTooltip(context, mouseX, mouseY);
        }

    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragScrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        boolean mouseClicked = super.mouseClicked(mouseX, mouseY, button);
        if (cannotScroll()) {
            return mouseClicked;
        }

        this.dragScrolling = false;

        int scrollBarY = 36;
        int maxScrollBarOffset = 141;

        scrollBarY += (int) ((maxScrollBarOffset - scrollBarY) * (scrollPos / (float) currentMaxScroll));
        if (!canDragScroll(mouseX, mouseY, scrollBarY)) {
            return mouseClicked;
        }

        this.dragScrolling = true;
        this.scrollDragStart = scrollBarY;
        this.mouseDragStart = mouseY;

        return true;

    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {

        boolean mouseDragged = super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        if (!dragScrolling) {
            return mouseDragged;
        }

        int delta = (int) (mouseY - mouseDragStart);
        int newScrollPos = Math.max(36, Math.min(141, scrollDragStart + delta));

        float part = (newScrollPos - 36) / (float) (141 - 36);
        this.scrollPos = (int) (part * currentMaxScroll);

        return mouseDragged;

    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {

        int newScrollPos = this.scrollPos - (int) vertical * 4;
        this.scrollPos = MathHelper.clamp(newScrollPos, 0, this.currentMaxScroll);

        return super.mouseScrolled(x, y, horizontal, vertical);

    }

    public Origin getCurrentOrigin() {
        return origin;
    }

    public OriginLayer getCurrentLayer() {
        return layer;
    }

    protected void renderScrollbar(DrawContext context, int mouseX, int mouseY) {

        if (cannotScroll()) {
            return;
        }

        context.drawGuiTexture(WINDOW_SCROLL_BAR_SLOT, guiLeft + 155, guiTop + 35, 8, 134);

        int scrollbarY = 36;
        int maxScrollbarOffset = 141;

        scrollbarY += (int) ((maxScrollbarOffset - scrollbarY) * (scrollPos / (float) currentMaxScroll));

        Identifier scrollBarTexture = this.dragScrolling || canDragScroll(mouseX, mouseY, scrollbarY) ? WINDOW_SCROLL_BAR_PRESSED : WINDOW_SCROLL_BAR;
        context.drawGuiTexture(scrollBarTexture, guiLeft + 156, guiTop + scrollbarY, 6, 27);

    }

    protected boolean cannotScroll() {
        return origin == null || currentMaxScroll <= 0;
    }

    protected boolean canDragScroll(double mouseX, double mouseY, int scrollBarY) {
        return (mouseX >= guiLeft + 156 && mouseX < guiLeft + 156 + 6)
            && (mouseY >= guiTop + scrollBarY && mouseY < guiTop + scrollBarY + 27);
    }

    protected void renderBadgeTooltip(DrawContext context, int mouseX, int mouseY) {

        for (RenderedBadge renderedBadge : renderedBadges) {

            if (canRenderBadgeTooltip(renderedBadge, mouseX, mouseY)) {
                int widthLimit = width - mouseX - 24;
                ((DrawContextAccessor) context).invokeDrawTooltip(textRenderer, renderedBadge.getTooltipComponents(textRenderer, widthLimit), mouseX, mouseY, HoveredTooltipPositioner.INSTANCE);
            }

        }

    }

    protected boolean canRenderBadgeTooltip(RenderedBadge renderedBadge, int mouseX, int mouseY) {
        return renderedBadge.hasTooltip()
            && (mouseX >= renderedBadge.x && mouseX < renderedBadge.x + 9)
            && (mouseY >= renderedBadge.y && mouseY < renderedBadge.y + 9);
    }

    protected Text getTitleText() {
        return Text.of("Origins");
    }

    protected void renderOriginWindow(DrawContext context, int mouseX, int mouseY, float delta) {

        if (origin != null) {
            //context.enableScissor(guiLeft, guiTop, guiLeft + windowWidth, guiTop + windowHeight);
            this.renderOriginContent(context);
            //context.disableScissor();
        }

        context.drawGuiTexture(WINDOW_BORDER, guiLeft, guiTop, 2, WINDOW_WIDTH, WINDOW_HEIGHT);
        context.drawGuiTexture(WINDOW_NAME_PLATE, guiLeft + 10, guiTop + 10, 2, 150, 26);

        if (origin != null) {

            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 5);

            this.renderOriginName(context, mouseX, mouseY, delta);
            this.renderOriginImpact(context, mouseX, mouseY);

            context.getMatrices().pop();
            context.drawCenteredTextWithShadow(this.textRenderer, getTitleText(), width / 2, guiTop - 15, 0xFFFFFF);

        }

    }

    protected void renderOriginImpact(DrawContext context, int mouseX, int mouseY) {

        Impact impact = origin.getImpact();
        context.drawGuiTexture(impact.getSpriteId(), guiLeft + 128, guiTop + 19, 2, 28, 8);

        if (this.isHoveringOverImpact(mouseX, mouseY)) {
            MutableText impactHoverTooltip = Text.translatable(Origins.MODID + ".gui.impact.impact").append(": ").append(impact.getTextComponent());
            context.drawTooltip(this.textRenderer, impactHoverTooltip, mouseX, mouseY);
        }

    }

    protected boolean isHoveringOverImpact(int mouseX, int mouseY) {
        return (mouseX >= guiLeft + 128 && mouseX <= guiLeft + 158)
            && (mouseY >= guiTop + 19 && mouseY <= guiTop + 27);
    }

    protected void renderOriginName(DrawContext context, int mouseX, int mouseY, float delta) {

        if (refreshOriginNameWidget || (origin != prevOrigin || layer != prevLayer)) {

            Text name = origin == Origin.EMPTY && layer != null && layer.getMissingName() != null ? layer.getMissingName() : origin.getName();

            originNameWidget = new ScrollingTextWidget(guiLeft + 38, guiTop + 18, WINDOW_WIDTH - (62 + 3 * 8), 9, name, true, textRenderer);
            originNameWidget.setAlignment(TextAlignment.LEFT);

            refreshOriginNameWidget = false;

            prevOrigin = origin;
            prevLayer = layer;

        }

        originNameWidget.render(context, mouseX, mouseY, delta);

        ItemStack iconStack = getCurrentOrigin().getDisplayItem();
        context.drawItem(iconStack, guiLeft + 15, guiTop + 15);

    }

    protected void renderOriginContent(DrawContext context) {

        int textWidthLimit = WINDOW_WIDTH - 48;

        /*
            Without this code, the text may not cover the whole width of the window if the scroll bar isn't shown. However, with this code,
            you'll see 1 frame of misaligned text because the text length (and whether scrolling is enabled) is only evaluated on
            first render :(
         */

//        if (cannotScroll()) {
//            textWidth += 12;
//        }

        int x = guiLeft + 18;
        int y = guiTop + 50;
        int startY = y;
        int endY = y - 72 + WINDOW_HEIGHT;

        y -= scrollPos;

        Text description = origin == Origin.EMPTY && layer != null && layer.getMissingDescription() != null ? layer.getMissingDescription() : origin.getDescription();
        for (OrderedText descriptionLine : textRenderer.wrapLines(description, textWidthLimit)) {

            if (y >= startY - 24 && y <= endY + 12) {
                context.drawTextWithShadow(textRenderer, descriptionLine, x + 2, y, 0xCCCCCC);
            }

            y += 12;

        }

        y += 12;
        if (isOriginRandom) {

            for (OrderedText randomOriginLine : textRenderer.wrapLines(randomOriginText, textWidthLimit)) {

                y += 12;

                if (y >= startY - 24 && y <= endY + 12) {
                    context.drawTextWithShadow(textRenderer, randomOriginLine, x + 2, y, 0xCCCCCC);
                }

            }

            y += 14;

        } else {

            for (PowerType<?> power : origin.getPowerTypes()) {

                if (power.isHidden()) {
                    continue;
                }

                LinkedList<OrderedText> powerName = new LinkedList<>(textRenderer.wrapLines(power.getName().formatted(Formatting.UNDERLINE), textWidthLimit));
                int powerNameWidth = textRenderer.getWidth(powerName.getLast());

                for (OrderedText powerNameLine : powerName) {

                    if (y >= startY - 24 && y <= endY + 12) {
                        context.drawTextWithShadow(textRenderer, powerNameLine, x, y, 0xFFFFFF);
                    }

                    y += 12;

                }

                y -= 12;

                int badgeStartX = x + powerNameWidth + 4;
                int badgeEndX = x + 135;

                int badgeOffsetX = 0;
                int badgeOffsetY = 0;

                for (PowerType<?> subOrSelfPower : this.getSubPowersOrSelf(power, BadgeManager::hasPowerBadges)) {

                    for (Badge badge : BadgeManager.getPowerBadges(subOrSelfPower.getIdentifier())) {

                        int badgeX = badgeStartX + 10 * badgeOffsetX;
                        int badgeY = (y - 1) + 10 * badgeOffsetY;

                        if (badgeX >= badgeEndX) {

                            badgeOffsetX = 0;
                            badgeOffsetY++;

                            badgeX = badgeStartX = x;
                            badgeY = (y - 1) + 10 * badgeOffsetY;

                        }

                        if (badgeY >= startY - 34 && badgeY <= endY + 12) {

                            RenderedBadge renderedBadge = new RenderedBadge(subOrSelfPower, badge, badgeX, badgeY);
                            renderedBadges.add(renderedBadge);

                            context.drawTexture(badge.spriteId(), renderedBadge.x, renderedBadge.y, -2, 0, 0, 9, 9, 9, 9);

                        }

                        badgeOffsetX++;

                    }

                }

                y += badgeOffsetY * 10;
                for (OrderedText powerDescriptionLine : textRenderer.wrapLines(power.getDescription(), textWidthLimit)) {

                    y += 12;

                    if (y >= startY - 24 && y <= endY + 12) {
                        context.drawTextWithShadow(textRenderer, powerDescriptionLine, x + 2, y, 0xCCCCCC);
                    }

                }

                y += 20;

            }

        }

        y += scrollPos;
        currentMaxScroll = Math.max(0, y - 14 - (guiTop + 158));

    }

    protected final List<PowerType<?>> getSubPowersOrSelf(PowerType<?> powerType, Predicate<PowerType<?>> selfPredicate) {

        if (!(powerType instanceof MultiplePowerType<?> multiplePowerType) || selfPredicate.test(multiplePowerType)) {
            return List.of(powerType);
        }

        List<PowerType<?>> subPowers = new LinkedList<>();
        for (Identifier subPowerTypeId : multiplePowerType.getSubPowers()) {

            PowerType<?> subPowerType = PowerTypeRegistry.getNullable(subPowerTypeId);

            if (subPowerType != null) {
                subPowers.add(subPowerType);
            }

        }

        return subPowers;

    }

    protected class RenderedBadge {

        private final PowerType<?> powerType;
        private final Badge badge;

        private final int x;
        private final int y;

        public RenderedBadge(PowerType<?> powerType, Badge badge, int x, int y) {
            this.powerType = powerType;
            this.badge = badge;
            this.x = x;
            this.y = y;
        }

        public List<TooltipComponent> getTooltipComponents(TextRenderer textRenderer, int widthLimit) {
            return badge.getTooltipComponents(powerType, widthLimit, OriginDisplayScreen.this.time, textRenderer);
        }

        public boolean hasTooltip() {
            return badge.hasTooltip();
        }

    }

}
