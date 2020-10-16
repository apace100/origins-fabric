package io.github.apace100.origins.networking;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PowerTypeRegistry;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import io.github.apace100.origins.util.SerializableData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(ModPackets.OPEN_ORIGIN_SCREEN, (packetContext, packetByteBuf) -> {
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
        });
        ClientSidePacketRegistry.INSTANCE.register(ModPackets.ORIGIN_LIST, (packetContext, packetByteBuf) -> {
            Identifier[] ids = new Identifier[packetByteBuf.readInt()];
            SerializableData.Instance[] origins = new SerializableData.Instance[ids.length];
            for(int i = 0; i < origins.length; i++) {
                ids[i] = Identifier.tryParse(packetByteBuf.readString());
                origins[i] = Origin.DATA.read(packetByteBuf);
            }
            packetContext.getTaskQueue().execute(() -> {
                OriginRegistry.reset();
                for(int i = 0; i < ids.length; i++) {
                    OriginRegistry.register(ids[i], Origin.createFromData(ids[i], origins[i]));
                }
            });
        });
        ClientSidePacketRegistry.INSTANCE.register(ModPackets.LAYER_LIST, (packetContext, packetByteBuf) -> {
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
        });
        ClientSidePacketRegistry.INSTANCE.register(ModPackets.POWER_LIST, (packetContext, packetByteBuf) -> {
            int powerCount = packetByteBuf.readInt();
            HashMap<Identifier, PowerType> factories = new HashMap<>();
            for(int i = 0; i < powerCount; i++) {
                Identifier powerId = packetByteBuf.readIdentifier();
                Identifier factoryId = packetByteBuf.readIdentifier();
                PowerFactory factory = ModRegistries.POWER_FACTORY.get(factoryId);
                PowerFactory.Instance factoryInstance = factory.read(packetByteBuf);
                PowerType type = new PowerType(powerId, factoryInstance);
                type.setTranslationKeys(packetByteBuf.readString(), packetByteBuf.readString());
                if(packetByteBuf.readBoolean()) {
                    type.setHidden();
                }
                factories.put(powerId, type);
            }
            packetContext.getTaskQueue().execute(() -> {
                PowerTypeRegistry.clear();
                factories.forEach(PowerTypeRegistry::register);
            });
        });
    }
}
