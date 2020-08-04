package io.github.apace100.origins.networking;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(ModPackets.OPEN_ORIGIN_SCREEN, ((packetContext, packetByteBuf) -> {
            boolean showDirtBackground = packetByteBuf.readBoolean();
            packetContext.getTaskQueue().execute(() -> {
                ArrayList<OriginLayer> layers = new ArrayList<>();
                OriginComponent component = ModComponents.ORIGIN.get(MinecraftClient.getInstance().player);
                OriginLayers.getLayers().forEach(layer -> {
                    if(layer.isEnabled() && !component.hasOrigin(layer)) {
                        layers.add(layer);
                    }
                });
                Collections.sort(layers);
                MinecraftClient.getInstance().openScreen(new ChooseOriginScreen(layers, 0, showDirtBackground));
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
        ClientSidePacketRegistry.INSTANCE.register(ModPackets.LAYER_LIST, ((((packetContext, packetByteBuf) -> {
            int layerCount = packetByteBuf.readInt();
            OriginLayer[] layers = new OriginLayer[layerCount];
            for(int i = 0; i < layerCount; i++) {
                layers[i] = OriginLayer.read(packetByteBuf);
            }
            packetContext.getTaskQueue().execute(() -> {
                OriginLayers.clear();
                for(int i = 0; i < layerCount; i++) {
                    OriginLayers.add(layers[i]);
                }
            });
        }))));
    }
}
