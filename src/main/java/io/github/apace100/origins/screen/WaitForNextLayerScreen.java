package io.github.apace100.origins.screen;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class WaitForNextLayerScreen extends Screen {

    private final ArrayList<OriginLayer> layerList;
    private final int currentLayerIndex;
    private final boolean showDirtBackground;
    private final int maxSelection;

    protected WaitForNextLayerScreen(ArrayList<OriginLayer> layerList, int currentLayerIndex, boolean showDirtBackground) {
        super(Text.empty());
        this.layerList = layerList;
        this.currentLayerIndex = currentLayerIndex;
        this.showDirtBackground = showDirtBackground;
        PlayerEntity player = MinecraftClient.getInstance().player;
        OriginLayer currentLayer = layerList.get(currentLayerIndex);
        maxSelection = currentLayer.getOriginOptionCount(player);
    }

    public void openSelection() {
        int index = currentLayerIndex + 1;
        PlayerEntity player = MinecraftClient.getInstance().player;
        OriginComponent component = ModComponents.ORIGIN.get(player);
        while(index < layerList.size()) {
            if(!component.hasOrigin(layerList.get(index)) && layerList.get(index).getOrigins(player).size() > 0) {
                MinecraftClient.getInstance().setScreen(new ChooseOriginScreen(layerList, index, showDirtBackground));
                return;
            }
            index++;
        }
        MinecraftClient.getInstance().setScreen(null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if(maxSelection == 0) {
            openSelection();
            return;
        }
        this.renderBackground(context);
    }

    @Override
    public void renderBackground(DrawContext context) {
        if(showDirtBackground) {
            super.renderBackgroundTexture(context);
        } else {
            super.renderBackground(context);
        }
    }
}
