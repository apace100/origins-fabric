package io.github.apace100.origins.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.power.MultiplePower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.screen.widget.ScrollingTextWidget;
import io.github.apace100.apoli.util.TextAlignment;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.mixin.DrawContextAccessor;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginManager;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.function.Predicate;

//  TODO: Use a custom widget for the origin window -eggohito
public class OriginDisplayScreen extends Screen {

    public static final Identifier DIRT_BACKGROUND = Origins.identifier("textures/dirt_background.png");

    private static final Identifier WINDOW_BACKGROUND = Origins.identifier("choose_origin/background");
    private static final Identifier WINDOW_BORDER = Origins.identifier("choose_origin/border");
    private static final Identifier WINDOW_NAME_PLATE = Origins.identifier("choose_origin/name_plate");
    private static final Identifier WINDOW_SCROLL_BAR = Origins.identifier("choose_origin/scroll_bar");
    private static final Identifier WINDOW_SCROLL_BAR_PRESSED = Origins.identifier("choose_origin/scroll_bar/pressed");
    private static final Identifier WINDOW_SCROLL_BAR_SLOT = Origins.identifier("choose_origin/scroll_bar/slot");

    private static final int MIN_SCROLL_BAR_Y = 36;
    private static final int MAX_SCROLL_BAR_Y = 141;

    protected static final int WINDOW_WIDTH = 176;
    protected static final int WINDOW_HEIGHT = 182;

    protected final boolean showDirtBackground;

    private Origin origin;
    private Origin prevOrigin;

    private OriginLayer layer;
    private OriginLayer prevLayer;

    protected ScrollingTextWidget nameWidget;
    protected Text randomDescription;

    private boolean isRandom;
    private boolean dragScrolling = false;
    private boolean refreshOriginNameWidget = false;

    private double mouseYDragStart = 0;
    private int scrollYDragStart = 0;

    protected int guiTop, guiLeft;
    protected int maxScroll, scrollPos;

    public OriginDisplayScreen(Text title, boolean showDirtBackground) {
        super(title);
        this.showDirtBackground = showDirtBackground;
    }

    protected void showOrigin(Origin origin, OriginLayer layer) {
        this.origin = origin;
        this.layer = layer;
        this.isRandom = origin == Origin.RANDOM;
        this.scrollPos = 0;
    }

    protected void initRandomDescription() {

        assert client != null && client.player != null;

        MutableText description = Text.of("").copy();
        ObjectSortedSet<Origin> randoms = new ObjectAVLTreeSet<>(Origin::compareTo);

        for (var originId : getCurrentLayer().getRandomOrigins(client.player)) {
            randoms.add(OriginManager.get(originId));
        }

        for (var random : randoms) {
            description.append(random.getName()).append(Text.of("\n"));
        }

        this.randomDescription = description;

    }

    @Override
    protected void init() {

        super.init();

        this.origin = null;
        this.prevOrigin = null;

        this.layer = null;
        this.prevLayer = null;

        this.guiLeft = (this.width - WINDOW_WIDTH) / 2;
        this.guiTop = (this.height - WINDOW_HEIGHT) / 2;

        this.nameWidget = new ScrollingTextWidget(guiLeft + 38, guiTop + 18, WINDOW_WIDTH - (62 + 3 * 8), 9, Text.empty(), true, textRenderer);
        this.refreshOriginNameWidget = true;

    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {

        if (showDirtBackground) {
            RenderSystem.enableBlend();
            context.drawTexture(DIRT_BACKGROUND, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
            RenderSystem.disableBlend();
        }

        else {
            super.renderBackground(context, mouseX, mouseY, delta);
        }

    }

    @Override
    public void renderInGameBackground(DrawContext context) {
        context.fillGradient(0, 0, this.width, this.height, 1678774288, -2112876528);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.renderWindow(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragScrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        boolean mouseClicked = super.mouseClicked(mouseX, mouseY, button);
        int scrollBarY = MIN_SCROLL_BAR_Y + (int) Math.floor((MAX_SCROLL_BAR_Y - MIN_SCROLL_BAR_Y) * (scrollPos / (float) maxScroll));

        if (this.cannotScroll() || !this.canDragScroll(mouseX, mouseY, scrollBarY)) {
            return mouseClicked;
        }

        this.scrollYDragStart = scrollBarY;
        this.mouseYDragStart = mouseY;
        this.dragScrolling = true;

        return mouseClicked;

    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {

        boolean mouseDragged = super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

        if (!dragScrolling) {
            return mouseDragged;
        }

        int delta = (int) Math.floor(mouseY - mouseYDragStart);
        int newScrollPos = MathHelper.clamp(scrollYDragStart + delta, MIN_SCROLL_BAR_Y, MAX_SCROLL_BAR_Y);

        float part = (newScrollPos - MIN_SCROLL_BAR_Y) / (float) (MAX_SCROLL_BAR_Y - MIN_SCROLL_BAR_Y);
        this.scrollPos = (int) Math.floor(part * maxScroll);

        return mouseDragged;

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {

        boolean mouseScrolled = super.mouseScrolled(mouseX, mouseY, horizontal, vertical);

        if (cannotScroll()) {
            return mouseScrolled;
        }

        int scrollBarY = MIN_SCROLL_BAR_Y + (int) Math.floor((MAX_SCROLL_BAR_Y - MIN_SCROLL_BAR_Y) * (scrollPos / (float) maxScroll));
        int delta = (int) Math.floor(vertical) * (hasShiftDown() ? 4 : 12);

        int newScrollPos = MathHelper.clamp(scrollBarY - delta, MIN_SCROLL_BAR_Y, MAX_SCROLL_BAR_Y);
        float part = (newScrollPos - MIN_SCROLL_BAR_Y) / (float) (MAX_SCROLL_BAR_Y - MIN_SCROLL_BAR_Y);

        this.scrollPos = (int) Math.floor(part * maxScroll);
        return mouseScrolled;

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

        int scrollbarY = MIN_SCROLL_BAR_Y + (int) Math.floor((MAX_SCROLL_BAR_Y - MIN_SCROLL_BAR_Y) * (scrollPos / (float) maxScroll));
        context.drawGuiTexture(WINDOW_SCROLL_BAR_SLOT, guiLeft + 155, guiTop + 35, 8, 134);

        Identifier scrollBarTexture = this.dragScrolling || this.canDragScroll(mouseX, mouseY, scrollbarY) ? WINDOW_SCROLL_BAR_PRESSED : WINDOW_SCROLL_BAR;
        context.drawGuiTexture(scrollBarTexture, guiLeft + 156, guiTop + scrollbarY, 6, 27);

    }

    protected boolean cannotScroll() {
        return origin == null || maxScroll <= 0;
    }

    protected boolean canDragScroll(double mouseX, double mouseY, int scrollBarY) {
        return (mouseX >= guiLeft + 156 && mouseX < guiLeft + 156 + 6)
            && (mouseY >= guiTop + scrollBarY && mouseY < guiTop + scrollBarY + 27);
    }

    protected boolean isWithinWindowBoundaries(int mouseX, int mouseY) {
        return (mouseX >= guiLeft && mouseX < guiLeft + WINDOW_WIDTH)
            && (mouseY >= guiTop && mouseY < guiTop + WINDOW_HEIGHT);
    }

    protected Text getTitleText() {
        return Text.of("Origins");
    }

    protected void renderTitle(DrawContext context) {
        context.drawCenteredTextWithShadow(this.textRenderer, getTitleText(), width / 2, guiTop - 15, 0xFFFFFF);
    }

    protected void renderWindow(DrawContext context, int mouseX, int mouseY, float delta) {

        context.drawGuiTexture(WINDOW_BACKGROUND, guiLeft, guiTop, WINDOW_WIDTH, WINDOW_HEIGHT);
        context.drawGuiTexture(WINDOW_BORDER, guiLeft, guiTop, 2, WINDOW_WIDTH, WINDOW_HEIGHT);

        if (origin == null) {
            return;
        }

        renderDescriptionAndBadges(context, mouseX, mouseY, delta);
        renderNameAndImpact(context, mouseX, mouseY, delta);

        renderScrollbar(context, mouseX, mouseY);
        renderTitle(context);

    }

    protected boolean isWithinImpactBoundaries(int mouseX, int mouseY) {

        int impactStartX = guiLeft + 128;
        int impactStartY = guiTop + 19;
        int impactEndX = impactStartX + 28;
        int impactEndY = impactStartY + 8;

        return (mouseX >= impactStartX && mouseX < impactEndX)
            && (mouseY >= impactStartY && mouseY < impactEndY);

    }

    protected void renderNameAndImpact(DrawContext context, int mouseX, int mouseY, float delta) {

        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 1.0F);

        //region Render the origin's name
        context.drawGuiTexture(WINDOW_NAME_PLATE, guiLeft + 10, guiTop + 10, 150, 26);

        if (refreshOriginNameWidget || (origin != prevOrigin || layer != prevLayer)) {

            Text name = origin == Origin.EMPTY && layer != null && layer.getMissingName() != null
                ? layer.getMissingName()
                : origin.getName();

            nameWidget = new ScrollingTextWidget(guiLeft + 38, guiTop + 18, WINDOW_WIDTH - (62 + 3 * 8), 9, name, true, textRenderer);
            nameWidget.setAlignment(TextAlignment.LEFT);

            refreshOriginNameWidget = false;

            prevOrigin = origin;
            prevLayer = layer;

        }

        nameWidget.render(context, mouseX, mouseY, delta);

        ItemStack iconStack = getCurrentOrigin().getDisplayItem();
        context.drawItem(iconStack, guiLeft + 15, guiTop + 15);
        //endregion

        //region Render the origin's impact
        Impact impact = origin.getImpact();
        context.drawGuiTexture(impact.getSpriteId(), guiLeft + 128, guiTop + 19, 28, 8);

        if (this.isWithinWindowBoundaries(mouseX, mouseY) && this.isWithinImpactBoundaries(mouseX, mouseY)) {
            MutableText impactHoverTooltip = Text.translatable(Origins.MODID + ".gui.impact.impact").append(": ").append(impact.getTextComponent());
            context.drawTooltip(this.textRenderer, impactHoverTooltip, mouseX, mouseY);
        }
        //endregion

        context.getMatrices().pop();

    }

    protected void renderDescriptionAndBadges(DrawContext context, int mouseX, int mouseY, float delta) {

        List<RenderedBadge> toRenderTooltip = new ObjectArrayList<>();
        int textWidthLimit = WINDOW_WIDTH - 48;

        /*
            Without this code, the text may not cover the whole width of the window if the scroll bar isn't shown. However, with this code,
            you'll see 1 frame of misaligned text because the text length (and whether scrolling is enabled) is only evaluated on
            first render :(
         */

//        if (cannotScroll()) {
//            textWidthLimit += 12;
//        }

        int x = guiLeft + 18;
        int y = guiTop + 45;

        Text description = origin == Origin.EMPTY && layer != null && layer.getMissingDescription() != null ? layer.getMissingDescription() : origin.getDescription();
        y -= scrollPos;

        context.enableScissor(guiLeft, guiTop, guiLeft + WINDOW_WIDTH, guiTop + WINDOW_HEIGHT);

        for (OrderedText descriptionLine : textRenderer.wrapLines(description, textWidthLimit)) {
            context.drawTextWithShadow(textRenderer, descriptionLine, x + 2, y, 0xCCCCCC);
            y += 12;
        }

        y += 12;
        if (isRandom) {

            for (OrderedText randomOriginLine : textRenderer.wrapLines(randomDescription, textWidthLimit)) {
                y += 12;
                context.drawTextWithShadow(textRenderer, randomOriginLine, x + 2, y, 0xCCCCCC);
            }

            y += 14;

        }

        else {

            for (Power power : origin.getPowers()) {

                if (power.isHidden()) {
                    continue;
                }

                LinkedList<OrderedText> powerName = new LinkedList<>(textRenderer.wrapLines(power.getName().formatted(Formatting.UNDERLINE), textWidthLimit));
                int powerNameWidth = textRenderer.getWidth(powerName.getLast());

                for (OrderedText powerNameLine : powerName) {
                    context.drawTextWithShadow(textRenderer, powerNameLine, x, y, 0xFFFFFF);
                    y += 12;
                }

                y -= 12;

                int badgeStartX = x + powerNameWidth + 4;
                int badgeEndX = x + 135;

                int badgeOffsetX = 0;
                int badgeOffsetY = 0;

                for (Power selfOrSubPower : this.getSelfOrSubPowers(power, BadgeManager::hasPowerBadges)) {

                    for (Badge badge : BadgeManager.getPowerBadges(selfOrSubPower.getId())) {

                        int badgeX = badgeStartX + 10 * badgeOffsetX;
                        int badgeY = (y - 1) + 10 * badgeOffsetY;

                        if (badgeX >= badgeEndX) {

                            badgeOffsetX = 0;
                            badgeOffsetY++;

                            badgeX = badgeStartX = x;
                            badgeY = (y - 1) + 10 * badgeOffsetY;

                        }

                        RenderedBadge renderedBadge = new RenderedBadge(selfOrSubPower, badge, badgeX, badgeY);
                        context.drawTexture(badge.spriteId(), renderedBadge.x(), renderedBadge.y(), 0, 0, 9, 9, 9, 9);

                        badgeOffsetX++;

                        if (this.isWithinWindowBoundaries(mouseX, mouseY) && renderedBadge.hasTooltip() && renderedBadge.withinBoundaries(mouseX, mouseY)) {
                            toRenderTooltip.add(renderedBadge);
                        }

                    }

                }

                y += badgeOffsetY * 10;

                for (OrderedText powerDescriptionLine : textRenderer.wrapLines(power.getDescription(), textWidthLimit)) {
                    y += 12;
                    context.drawTextWithShadow(textRenderer, powerDescriptionLine, x + 2, y, 0xCCCCCC);
                }

                y += 20;

            }

        }

        context.disableScissor();

        for (var badge : toRenderTooltip) {

            MatrixStack matrices = context.getMatrices();
            List<TooltipComponent> tooltipComponents = badge.getTooltipComponents(this.textRenderer, this.width - mouseX - 24, delta);

            matrices.push();
            matrices.translate(0.0F, 0.0F, 5.0F);

            ((DrawContextAccessor) context).invokeDrawTooltip(this.textRenderer, tooltipComponents, mouseX, mouseY, HoveredTooltipPositioner.INSTANCE);
            matrices.pop();

        }

        y += scrollPos;
        this.maxScroll = Math.max(0, y - 14 - (guiTop + 158));

    }

    protected final Collection<? extends Power> getSelfOrSubPowers(Power power, Predicate<Power> selfPredicate) {

        if (!selfPredicate.test(power) && power instanceof MultiplePower multiplePower) {
            return multiplePower.getSubPowers();
        }

        else {
            return Set.of(power);
        }

    }

    protected record RenderedBadge(Power power, Badge badge, int x, int y) {

        public List<TooltipComponent> getTooltipComponents(TextRenderer textRenderer, int widthLimit, float delta) {
            return badge.getTooltipComponents(power, widthLimit, delta, textRenderer);
        }

        public boolean withinBoundaries(int mouseX, int mouseY) {
            return (mouseX >= x() && mouseX < x() + width())
                && (mouseY >= y() && mouseY < y() + height());
        }

        public boolean hasTooltip() {
            return badge.hasTooltip();
        }

        public int width() {
            return 9;
        }

        public int height() {
            return 9;
        }

    }

}
