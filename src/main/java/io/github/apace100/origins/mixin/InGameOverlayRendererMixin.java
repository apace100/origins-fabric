package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.PhasingPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

    @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
    private static void preventInWallOverlayRendering(MinecraftClient minecraftClient, Sprite sprite, MatrixStack matrixStack, CallbackInfo ci) {
        if(minecraftClient.cameraEntity != null) {
            if(OriginComponent.getPowers(minecraftClient.cameraEntity, PhasingPower.class).size() > 0) {
                ci.cancel();
            }
        }
    }
}
