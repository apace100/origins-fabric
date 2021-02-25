package io.github.apace100.origins.mixin;

import io.github.apace100.origins.OriginsClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {

    @Inject(method = "connect", at = @At("HEAD"))
    private void resetServerOriginsState(String address, int port, CallbackInfo ci) {
        OriginsClient.isServerRunningOrigins = false;
    }
}
