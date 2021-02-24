package io.github.apace100.origins.screen;

import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;

public class WaitForNextLayerScreen extends Screen {

    private final ArrayList<OriginLayer> layerList;
    private final int currentLayerIndex;
    private final boolean showDirtBackground;

    protected WaitForNextLayerScreen(ArrayList<OriginLayer> layerList, int currentLayerIndex, boolean showDirtBackground) {
        super(LiteralText.EMPTY);
        this.layerList = layerList;
        this.currentLayerIndex = currentLayerIndex;
        this.showDirtBackground = showDirtBackground;
    }

    public void openSelection() {
        int index = currentLayerIndex + 1;
        PlayerEntity player = MinecraftClient.getInstance().player;
        while(index < layerList.size()) {
            if(layerList.get(index).getOrigins(player).size() > 0) {
                MinecraftClient.getInstance().openScreen(new ChooseOriginScreen(layerList, index, showDirtBackground));
                return;
            }
            index++;
        }
        MinecraftClient.getInstance().openScreen(null);
    }

    @Override
    public void renderBackground(MatrixStack matrices, int vOffset) {
        if(showDirtBackground) {
            super.renderBackgroundTexture(vOffset);
        } else {
            super.renderBackground(matrices, vOffset);
        }
    }
}
