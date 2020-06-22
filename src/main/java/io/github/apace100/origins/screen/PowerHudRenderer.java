package io.github.apace100.origins.screen;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.HudRendered;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public class PowerHudRenderer extends DrawableHelper {

    public static final Identifier OVERLAY_TEXTURE = new Identifier(Origins.MODID, "textures/gui/resource_bar.png");

    @Environment(EnvType.CLIENT)
    public void register() {
        HudRenderCallback.EVENT.register(((matrices, delta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            OriginComponent component = ModComponents.ORIGIN.get(client.player);
            if(component.hasOrigin()) {
                int x = client.getWindow().getScaledWidth() / 2 + 20;
                int y = client.getWindow().getScaledHeight() - 48;
                if(client.player.getAir() < client.player.getMaxAir()) {
                    y -= 8;
                }
                if(client.player.isCreative()) {
                    y += 8;
                }
                int barWidth = 71;
                int barHeight = 5;
                int iconSize = 8;
                List<HudRendered> hudPowers = component.getPowers().stream().filter(p -> p instanceof HudRendered).map(p -> (HudRendered)p).collect(Collectors.toList());
                if(hudPowers.size() > 0) {
                    client.getTextureManager().bindTexture(OVERLAY_TEXTURE);
                }
                for (HudRendered hudPower : hudPowers) {
                    if(hudPower.shouldRender()) {
                        drawTexture(matrices, x, y, 0, 0, barWidth, barHeight);
                        int v = 10 + hudPower.getBarIndex() * 10;
                        int w = (int)(hudPower.getFill() * barWidth);
                        drawTexture(matrices, x, y, 0, v, w, barHeight);
                        setZOffset(getZOffset() + 1);
                        drawTexture(matrices, x - iconSize - 2, y - 2, 73, v - 2, iconSize, iconSize);
                        setZOffset(getZOffset() - 1);
                        y -= 8;
                    }
                }
            }
        }));
    }
}
