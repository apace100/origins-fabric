package io.github.apace100.origins.networking;

import io.github.apace100.origins.screen.ChooseOriginScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(ModPackets.OPEN_ORIGIN_SCREEN, ((packetContext, packetByteBuf) -> {
            packetContext.getTaskQueue().execute(() -> {
                MinecraftClient.getInstance().openScreen(new ChooseOriginScreen());
            });
        }));
    }
}
