package io.github.apace100.origins.mixin;

import io.github.apace100.origins.OriginsClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ConnectScreen.class)
public class ServerCheckMixin {

    @Inject(method = "connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;)V", at = @At("HEAD"))
    private void resetServerOriginsState(MinecraftClient client, ServerAddress address, CallbackInfo ci) {
        OriginsClient.isServerRunningOrigins = false;
    }
}
