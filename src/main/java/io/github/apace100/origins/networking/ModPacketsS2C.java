package io.github.apace100.origins.networking;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(ModPackets.OPEN_ORIGIN_SCREEN, ((packetContext, packetByteBuf) -> {
            packetContext.getTaskQueue().execute(() -> {
                MinecraftClient.getInstance().openScreen(new ChooseOriginScreen());
            });
        }));
        ClientSidePacketRegistry.INSTANCE.register(ModPackets.ORIGIN_LIST, (((packetContext, packetByteBuf) -> {
            Identifier[] ids = new Identifier[packetByteBuf.readInt()];
            Origin[] origins = new Origin[ids.length];
            for(int i = 0; i < origins.length; i++) {
                ids[i] = Identifier.tryParse(packetByteBuf.readString());
                origins[i] = Origin.read(packetByteBuf);
            }
            packetContext.getTaskQueue().execute(() -> {
                OriginRegistry.reset();
                for(int i = 0; i < ids.length; i++) {
                    OriginRegistry.register(ids[i], origins[i]);
                }
            });
        })));
    }
}
