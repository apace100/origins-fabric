package io.github.apace100.origins.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.screen.widget.OriginWindowWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public abstract class OriginDisplayScreen extends Screen {

    public static final Identifier DIRT_BACKGROUND = Origins.identifier("textures/dirt_background.png");

    protected final OriginWindowWidget windowWidget;
    protected final boolean showDirtBackground;

    public OriginDisplayScreen(Text title, boolean showDirtBackground) {
        super(title);
        this.windowWidget = new OriginWindowWidget();
        this.showDirtBackground = showDirtBackground;
    }

    @Override
    protected void init() {

        super.init();

        this.windowWidget.init(this.textRenderer);
        this.windowWidget.setPosition((this.width - windowWidget.getWidth()) / 2, (this.height - windowWidget.getHeight()) / 2);

        this.addDrawableChild(windowWidget);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, this.getTitle(), width / 2, windowWidget.getY() - 15, 0xFFFFFF);
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

    protected abstract Origin getCurrentOrigin();

    protected abstract OriginLayer getCurrentLayer();

    protected void showCurrent(Function<Origin, Origin.WindowTextures> texturesGetter) {
        windowWidget.show(getCurrentOrigin(), getCurrentLayer(), texturesGetter);
    }

    protected void resetAndShowCurrent(Function<Origin, Origin.WindowTextures> texturesGetter) {
        windowWidget.resetAndShow(getCurrentOrigin(), getCurrentLayer(), texturesGetter);
    }

}
