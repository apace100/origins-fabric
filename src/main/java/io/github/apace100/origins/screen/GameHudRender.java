package io.github.apace100.origins.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public interface GameHudRender {

    List<GameHudRender> HUD_RENDERS = new ArrayList<>();

    void render(MatrixStack matrixStack, float tickDelta);
}
