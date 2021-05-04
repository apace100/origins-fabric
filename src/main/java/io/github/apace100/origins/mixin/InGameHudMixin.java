package io.github.apace100.origins.mixin;

import io.github.apace100.origins.screen.GameHudRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void renderOnHud(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        for(GameHudRender hudRender : GameHudRender.HUD_RENDERS) {
            hudRender.render(matrices, tickDelta);
        }
    }
}
