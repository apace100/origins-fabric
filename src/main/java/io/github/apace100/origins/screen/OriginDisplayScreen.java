package io.github.apace100.origins.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.mixin.ScreenAccessor;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.util.LinkedList;
import java.util.List;

public class OriginDisplayScreen extends Screen {

    private static final Identifier WINDOW = new Identifier(Origins.MODID, "textures/gui/choose_origin.png");
    private Origin origin;
    private OriginLayer layer;
    private boolean isOriginRandom;
    private Text randomOriginText;

    protected static final int windowWidth = 176;
    protected static final int windowHeight = 182;
    protected int scrollPos = 0;
    private int currentMaxScroll = 0;
    private float time = 0;

    protected int guiTop, guiLeft;

    protected final boolean showDirtBackground;

    private final LinkedList<RenderedBadge> renderedBadges = new LinkedList<>();

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
        guiLeft = (this.width - windowWidth) / 2;
        guiTop = (this.height - windowHeight) / 2;
    }

    public Origin getCurrentOrigin() {
        return origin;
    }

    public OriginLayer getCurrentLayer() {
        return layer;
    }

    @Override
    public void renderBackground(MatrixStack matrices, int vOffset) {
        if(showDirtBackground) {
            super.renderBackgroundTexture(vOffset);
        } else {
            super.renderBackground(matrices, vOffset);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderedBadges.clear();
        this.time += delta;
        this.renderBackground(matrices);
        this.renderOriginWindow(matrices, mouseX, mouseY);
        super.render(matrices, mouseX, mouseY, delta);
        if(origin != null) {
            renderScrollbar(matrices, mouseX, mouseY);
            renderBadgeTooltip(matrices, mouseX, mouseY);
        }
    }

    private void renderScrollbar(MatrixStack matrices, int mouseX, int mouseY) {
        if(!canScroll()) {
            return;
        }
        RenderSystem.setShaderTexture(0, WINDOW);
        this.drawTexture(matrices, guiLeft + 155, guiTop + 35, 188, 24, 8, 134);
        int scrollbarY = 36;
        int maxScrollbarOffset = 141;
        int u = 176;
        float part = scrollPos / (float)currentMaxScroll;
        scrollbarY += (maxScrollbarOffset - scrollbarY) * part;
        if(scrolling) {
            u += 6;
        } else if(mouseX >= guiLeft + 156 && mouseX < guiLeft + 156 + 6) {
            if(mouseY >= guiTop + scrollbarY && mouseY < guiTop + scrollbarY + 27) {
                u += 6;
            }
        }
        this.drawTexture(matrices, guiLeft + 156, guiTop + scrollbarY, u, 24, 6, 27);
    }

    private boolean scrolling = false;
    private int scrollDragStart = 0;
    private double mouseDragStart = 0;

    private boolean canScroll() {
        return origin != null && currentMaxScroll > 0;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(canScroll()) {
            scrolling = false;
            int scrollbarY = 36;
            int maxScrollbarOffset = 141;
            float part = scrollPos / (float)currentMaxScroll;
            scrollbarY += (maxScrollbarOffset - scrollbarY) * part;
            if(mouseX >= guiLeft + 156 && mouseX < guiLeft + 156 + 6) {
                if(mouseY >= guiTop + scrollbarY && mouseY < guiTop + scrollbarY + 27) {
                    scrolling = true;
                    scrollDragStart = scrollbarY;
                    mouseDragStart = mouseY;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(this.scrolling) {
            int delta = (int)(mouseY - mouseDragStart);
            int newScrollPos = (int)Math.max(36, Math.min(141, scrollDragStart + delta));
            float part = (newScrollPos - 36) / (float)(141 - 36);
            scrollPos = (int)(part * currentMaxScroll);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void renderBadgeTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        for(RenderedBadge rb : renderedBadges) {
            if(mouseX >= rb.x &&
               mouseX < rb.x + 9 &&
               mouseY >= rb.y &&
               mouseY < rb.y + 9 &&
               rb.hasTooltip()) {
                int widthLimit = width - mouseX - 24;
                ((ScreenAccessor)this).invokeRenderTooltipFromComponents(matrices, rb.getTooltipComponents(textRenderer, widthLimit), mouseX, mouseY);
            }
        }
    }

    protected Text getTitleText() {
        return Text.of("Origins");
    }

    private void renderOriginWindow(MatrixStack matrices, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        renderWindowBackground(matrices, 16, 0);
        if(origin != null) {
            this.renderOriginContent(matrices, mouseX, mouseY);
        }
        RenderSystem.setShaderTexture(0, WINDOW);
        this.drawTexture(matrices, guiLeft, guiTop, 0, 0, windowWidth, windowHeight);
        if(origin != null) {
            renderOriginName(matrices);
            RenderSystem.setShaderTexture(0, WINDOW);
            this.renderOriginImpact(matrices, mouseX, mouseY);
            Text title = getTitleText();
            this.drawCenteredText(matrices, this.textRenderer, title.getString(), width / 2, guiTop - 15, 0xFFFFFF);
        }
        RenderSystem.disableBlend();
    }

    private void renderOriginImpact(MatrixStack matrices, int mouseX, int mouseY) {
        Impact impact = getCurrentOrigin().getImpact();
        int impactValue = impact.getImpactValue();
        int wOffset = impactValue * 8;
        for(int i = 0; i < 3; i++) {
            if(i < impactValue) {
                this.drawTexture(matrices, guiLeft + 128 + i * 10, guiTop + 19, windowWidth + wOffset, 16, 8, 8);
            } else {
                this.drawTexture(matrices, guiLeft + 128 + i * 10, guiTop + 19, windowWidth, 16, 8, 8);
            }
        }
        if(mouseX >= guiLeft + 128 && mouseX <= guiLeft + 158
            && mouseY >= guiTop + 19 && mouseY <= guiTop + 27) {
            MutableText ttc = Text.translatable(Origins.MODID + ".gui.impact.impact").append(": ").append(impact.getTextComponent());
            this.renderTooltip(matrices, ttc, mouseX, mouseY);
        }
    }

    private void renderOriginName(MatrixStack matrices) {
        StringVisitable originName = textRenderer.trimToWidth(getCurrentOrigin().getName(), windowWidth - 36);
        drawStringWithShadow(matrices, textRenderer, originName.getString(), guiLeft + 39, guiTop + 19, 0xFFFFFF);
        ItemStack is = getCurrentOrigin().getDisplayItem();
        this.itemRenderer.renderInGui(is, guiLeft + 15, guiTop + 15);
    }

    private void renderWindowBackground(MatrixStack matrices, int offsetYStart, int offsetYEnd) {
        int border = 13;
        int endX = guiLeft + windowWidth - border;
        int endY = guiTop + windowHeight - border;
        RenderSystem.setShaderTexture(0, WINDOW);
        for(int x = guiLeft; x < endX; x += 16) {
            for(int y = guiTop + offsetYStart; y < endY + offsetYEnd; y += 16) {
                this.drawTexture(matrices, x, y, windowWidth, 0, Math.max(16, endX - x), Math.max(16, endY + offsetYEnd - y));
            }
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double z) {
        boolean retValue = super.mouseScrolled(x, y, z);
        int np = this.scrollPos - (int)z * 4;
        this.scrollPos = np < 0 ? 0 : Math.min(np, this.currentMaxScroll);
        return retValue;
    }

    private void renderOriginContent(MatrixStack matrices, int mouseX, int mouseY) {

        int textWidth = windowWidth - 48;
        // Without this code, the text may not cover the whole width of the window
        // if the scrollbar isn't shown. However with this code, you'll see 1 frame
        // of misaligned text because the text length (and whether scrolling is enabled)
        // is only evaluated on first render. :(
        /*if(!canScroll()) {
            textWidth += 12;
        }*/

        Origin origin = getCurrentOrigin();
        int x = guiLeft + 18;
        int y = guiTop + 50;
        int startY = y;
        int endY = y - 72 + windowHeight;
        y -= scrollPos;

        Text orgDesc = origin.getDescription();
        List<OrderedText> descLines = textRenderer.wrapLines(orgDesc, textWidth);
        for(OrderedText line : descLines) {
            if(y >= startY - 18 && y <= endY + 12) {
                textRenderer.draw(matrices, line, x + 2, y - 6, 0xCCCCCC);
            }
            y += 12;
        }

        if(isOriginRandom) {
            List<OrderedText> drawLines = textRenderer.wrapLines(randomOriginText, textWidth);
            for(OrderedText line : drawLines) {
                y += 12;
                if(y >= startY - 24 && y <= endY + 12) {
                    textRenderer.draw(matrices, line, x + 2, y, 0xCCCCCC);
                }
            }
            y += 14;
        } else {
            for(PowerType<?> p : origin.getPowerTypes()) {
                if(p.isHidden()) {
                    continue;
                }
                OrderedText name = Language.getInstance().reorder(textRenderer.trimToWidth(p.getName().formatted(Formatting.UNDERLINE), textWidth));
                Text desc = p.getDescription();
                List<OrderedText> drawLines = textRenderer.wrapLines(desc, textWidth);
                if(y >= startY - 24 && y <= endY + 12) {
                    textRenderer.draw(matrices, name, x, y, 0xFFFFFF);
                    int tw = textRenderer.getWidth(name);
                    List<Badge> badges = BadgeManager.getPowerBadges(p.getIdentifier());
                    int xStart = x + tw + 4;
                    int bi = 0;
                    for(Badge badge : badges) {
                        RenderedBadge renderedBadge = new RenderedBadge(p, badge,xStart + 10 * bi, y - 1);
                        renderedBadges.add(renderedBadge);
                        RenderSystem.setShaderTexture(0, badge.spriteId());
                        drawTexture(matrices, xStart + 10 * bi, y - 1, 0, 0, 9, 9, 9, 9);
                        bi++;
                    }
                }
                for(OrderedText line : drawLines) {
                    y += 12;
                    if(y >= startY - 24 && y <= endY + 12) {
                        textRenderer.draw(matrices, line, x + 2, y, 0xCCCCCC);
                    }
                }

                y += 14;

            }
        }
        y += scrollPos;
        currentMaxScroll = y - 14 - (guiTop + 158);
        if(currentMaxScroll < 0) {
            currentMaxScroll = 0;
        }
    }

    private class RenderedBadge {
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

        public boolean hasTooltip() {
            return badge.hasTooltip();
        }

        public List<TooltipComponent> getTooltipComponents(TextRenderer textRenderer, int widthLimit) {
            return badge.getTooltipComponents(powerType, widthLimit, OriginDisplayScreen.this.time, textRenderer);
        }

    }

}
