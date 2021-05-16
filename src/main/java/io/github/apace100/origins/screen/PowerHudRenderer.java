package io.github.apace100.origins.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.HudRendered;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.util.HudRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PowerHudRenderer extends DrawableHelper implements GameHudRender {

    @Override
    @Environment(EnvType.CLIENT)
    public void render(MatrixStack matrices, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        OriginComponent component = ModComponents.ORIGIN.get(client.player);
        if(component.hasAllOrigins()) {
            int x = client.getWindow().getScaledWidth() / 2 + 20 + OriginsClient.config.xOffset;
            int y = client.getWindow().getScaledHeight() - 47 + OriginsClient.config.yOffset;
            Entity vehicle = client.player.getVehicle();
            if(vehicle instanceof LivingEntity && ((LivingEntity)vehicle).getMaxHealth() > 20F) {
                y -= 8;
            }
            if(client.player.isSubmergedIn(FluidTags.WATER) || client.player.getAir() < client.player.getMaxAir()) {
                y -= 8;
            }
            int barWidth = 71;
            int barHeight = 5;
            int iconSize = 8;
            List<HudRendered> hudPowers = component.getPowers().stream().filter(p -> p instanceof HudRendered).map(p -> (HudRendered)p).sorted(
                Comparator.comparing(hudRenderedA -> hudRenderedA.getRenderSettings().getSpriteLocation())
            ).collect(Collectors.toList());
            Identifier lastLocation = null;
            RenderSystem.color3f(1f, 1f, 1f);
            for (HudRendered hudPower : hudPowers) {
                HudRender render = hudPower.getRenderSettings();
                if(render.shouldRender(client.player) && hudPower.shouldRender()) {
                    Identifier currentLocation = render.getSpriteLocation();
                    if(currentLocation != lastLocation) {
                        client.getTextureManager().bindTexture(currentLocation);
                        lastLocation = currentLocation;
                    }
                    drawTexture(matrices, x, y, 0, 0, barWidth, barHeight);
                    int v = 10 + render.getBarIndex() * 10;
                    int w = (int)(hudPower.getFill() * barWidth);
                    drawTexture(matrices, x, y, 0, v, w, barHeight);
                    setZOffset(getZOffset() + 1);
                    drawTexture(matrices, x - iconSize - 2, y - 2, 73, v - 2, iconSize, iconSize);
                    setZOffset(getZOffset() - 1);
                    y -= 8;
                }
            }
        }
    }
}
