package io.github.apace100.origins.networking;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.integration.OriginDataLoadedCallback;
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
import io.github.apace100.origins.screen.WaitForNextLayerScreen;
import io.github.apace100.origins.util.SerializableData;
import me.shedaniel.architectury.annotations.ExpectPlatform;
import me.shedaniel.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        registerPlatformSpecificPackets();
        NetworkManager.registerReceiver(NetworkManager.s2c(), ModPackets.OPEN_ORIGIN_SCREEN, ModPacketsS2C::openOriginScreen);
        NetworkManager.registerReceiver(NetworkManager.s2c(), ModPackets.ORIGIN_LIST, ModPacketsS2C::receiveOriginList);
        NetworkManager.registerReceiver(NetworkManager.s2c(), ModPackets.LAYER_LIST, ModPacketsS2C::receiveLayerList);
        NetworkManager.registerReceiver(NetworkManager.s2c(), ModPackets.POWER_LIST, ModPacketsS2C::receivePowerList);
        NetworkManager.registerReceiver(NetworkManager.s2c(), ModPackets.CONFIRM_ORIGIN, ModPacketsS2C::receiveOriginConfirmation);
    }

    @ExpectPlatform
    private static void registerPlatformSpecificPackets() {
        throw new AssertionError();
    }

    @Environment(EnvType.CLIENT)
    private static void receiveOriginConfirmation(PacketByteBuf packetByteBuf, NetworkManager.PacketContext context) {
        OriginLayer layer = OriginLayers.getLayer(packetByteBuf.readIdentifier());
        Origin origin = OriginRegistry.get(packetByteBuf.readIdentifier());
        context.queue(() -> {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            OriginComponent component = ModComponents.getOriginComponent(minecraftClient.player);
            component.setOrigin(layer, origin);
            if(minecraftClient.currentScreen instanceof WaitForNextLayerScreen) {
                ((WaitForNextLayerScreen)minecraftClient.currentScreen).openSelection();
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static void openOriginScreen(PacketByteBuf packetByteBuf, NetworkManager.PacketContext context) {
        boolean showDirtBackground = packetByteBuf.readBoolean();
        context.queue(() -> {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            ArrayList<OriginLayer> layers = new ArrayList<>();
            OriginComponent component = ModComponents.getOriginComponent(minecraftClient.player);
            OriginLayers.getLayers().forEach(layer -> {
                if(layer.isEnabled() && !component.hasOrigin(layer)) {
                    layers.add(layer);
                }
            });
            Collections.sort(layers);
            minecraftClient.openScreen(new ChooseOriginScreen(layers, 0, showDirtBackground));
        });
    }

    @Environment(EnvType.CLIENT)
    private static void receiveOriginList(PacketByteBuf packetByteBuf, NetworkManager.PacketContext context) {
        Identifier[] ids = new Identifier[packetByteBuf.readInt()];
        SerializableData.Instance[] origins = new SerializableData.Instance[ids.length];
        for(int i = 0; i < origins.length; i++) {
            ids[i] = Identifier.tryParse(packetByteBuf.readString());
            origins[i] = Origin.DATA.read(packetByteBuf);
        }
        context.queue(() -> {
            OriginRegistry.reset();
            for(int i = 0; i < ids.length; i++) {
                OriginRegistry.register(ids[i], Origin.createFromData(ids[i], origins[i]));
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static void receiveLayerList(PacketByteBuf packetByteBuf, NetworkManager.PacketContext context) {
        int layerCount = packetByteBuf.readInt();
        OriginLayer[] layers = new OriginLayer[layerCount];
        for(int i = 0; i < layerCount; i++) {
            layers[i] = OriginLayer.read(packetByteBuf);
        }
        context.queue(() -> {
            OriginLayers.clear();
            for(int i = 0; i < layerCount; i++) {
                OriginLayers.add(layers[i]);
            }
            OriginDataLoadedCallback.EVENT.invoker().onDataLoaded(true);
        });
    }

    @Environment(EnvType.CLIENT)
    private static void receivePowerList(PacketByteBuf packetByteBuf, NetworkManager.PacketContext context) {
        int powerCount = packetByteBuf.readInt();
        HashMap<Identifier, PowerType<?>> factories = new HashMap<>();
        for(int i = 0; i < powerCount; i++) {
            Identifier powerId = packetByteBuf.readIdentifier();
            Identifier factoryId = packetByteBuf.readIdentifier();
            PowerFactory<?> factory = ModRegistries.POWER_FACTORY.get(factoryId);
            PowerFactory<?>.Instance factoryInstance = factory.read(packetByteBuf);
            PowerType<?> type = new PowerType<>(powerId, factoryInstance);
            type.setTranslationKeys(packetByteBuf.readString(), packetByteBuf.readString());
            if(packetByteBuf.readBoolean()) {
                type.setHidden();
            }
            factories.put(powerId, type);
        }
        context.queue(() -> {
            PowerTypeRegistry.clear();
            factories.forEach(PowerTypeRegistry::register);
        });
    }
}
