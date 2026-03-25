package io.github.apace100.origins.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class WaitForNextLayerScreen extends Screen {

    private final List<OriginLayer> layers;
    private final int index;

    private final boolean showDirtBackground;
    private int optionCount;

    protected WaitForNextLayerScreen(List<OriginLayer> layers, int index, boolean showDirtBackground) {
        super(Text.empty());
        this.layers = layers;
        this.index = index;
        this.showDirtBackground = showDirtBackground;
    }

    @Override
    protected void init() {

        super.init();
        assert client != null && client.player != null;

        this.optionCount = layers.get(index).getOriginOptionCount(client.player);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        if (optionCount == 0) {
            nextOrClose();
        }

        else {
            this.renderBackground(context, mouseX, mouseY, delta);
        }

    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {

        if (showDirtBackground) {
            RenderSystem.enableBlend();
            context.drawTexture(OriginDisplayScreen.DIRT_BACKGROUND, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
            RenderSystem.disableBlend();
        }

        else {
            super.renderBackground(context, mouseX, mouseY, delta);
        }

    }

    public void nextOrClose() {

        int layersCount = layers.size();
        assert client != null && client.player != null : "Tried iterating through " + layersCount + " layer(s) with the client and its player unset!";

        OriginComponent originComponent = ModComponents.ORIGIN.get(client.player);
        OriginLayer layer;

        for (int index = this.index + 1; index < layersCount; index++) {

            layer = layers.get(index);

            if (!originComponent.hasOrigin(layer) && !layer.getOrigins(client.player).isEmpty()) {
                client.setScreen(new ChooseOriginScreen(layers, index, showDirtBackground));
                return;
            }

        }

        client.setScreen(null);

    }

}
