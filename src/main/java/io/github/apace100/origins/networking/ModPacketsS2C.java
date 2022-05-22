package io.github.apace100.origins.networking;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.integration.OriginDataLoadedCallback;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import io.github.apace100.origins.screen.WaitForNextLayerScreen;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsS2C::handleHandshake);
        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(ModPackets.OPEN_ORIGIN_SCREEN, ModPacketsS2C::openOriginScreen);
            ClientPlayNetworking.registerReceiver(ModPackets.ORIGIN_LIST, ModPacketsS2C::receiveOriginList);
            ClientPlayNetworking.registerReceiver(ModPackets.LAYER_LIST, ModPacketsS2C::receiveLayerList);
            ClientPlayNetworking.registerReceiver(ModPackets.CONFIRM_ORIGIN, ModPacketsS2C::receiveOriginConfirmation);
            ClientPlayNetworking.registerReceiver(ModPackets.BADGE_LIST, ModPacketsS2C::receiveBadgeList);
        }));
    }

    @Environment(EnvType.CLIENT)
    private static void receiveOriginConfirmation(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        OriginLayer layer = OriginLayers.getLayer(packetByteBuf.readIdentifier());
        Origin origin = OriginRegistry.get(packetByteBuf.readIdentifier());
        minecraftClient.execute(() -> {
            OriginComponent component = ModComponents.ORIGIN.get(minecraftClient.player);
            component.setOrigin(layer, origin);
            if(minecraftClient.currentScreen instanceof WaitForNextLayerScreen) {
                ((WaitForNextLayerScreen)minecraftClient.currentScreen).openSelection();
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static CompletableFuture<PacketByteBuf> handleHandshake(MinecraftClient minecraftClient, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf packetByteBuf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(Origins.SEMVER.length);
        for(int i = 0; i < Origins.SEMVER.length; i++) {
            buf.writeInt(Origins.SEMVER[i]);
        }
        OriginsClient.isServerRunningOrigins = true;
        return CompletableFuture.completedFuture(buf);
    }

    @Environment(EnvType.CLIENT)
    private static void openOriginScreen(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        boolean showDirtBackground = packetByteBuf.readBoolean();
        minecraftClient.execute(() -> {
            ArrayList<OriginLayer> layers = new ArrayList<>();
            OriginComponent component = ModComponents.ORIGIN.get(minecraftClient.player);
            OriginLayers.getLayers().forEach(layer -> {
                if(layer.isEnabled() && !component.hasOrigin(layer)) {
                    layers.add(layer);
                }
            });
            Collections.sort(layers);
            minecraftClient.setScreen(new ChooseOriginScreen(layers, 0, showDirtBackground));
        });
    }

    @Environment(EnvType.CLIENT)
    private static void receiveOriginList(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        try {
            Identifier[] ids = new Identifier[packetByteBuf.readInt()];
            SerializableData.Instance[] origins = new SerializableData.Instance[ids.length];
            for(int i = 0; i < origins.length; i++) {
                ids[i] = Identifier.tryParse(packetByteBuf.readString());
                origins[i] = Origin.DATA.read(packetByteBuf);
            }
            minecraftClient.execute(() -> {
                OriginsClient.isServerRunningOrigins = true;
                OriginRegistry.reset();
                for(int i = 0; i < ids.length; i++) {
                    OriginRegistry.register(ids[i], Origin.createFromData(ids[i], origins[i]));
                }
            });
        } catch (Exception e) {
            Origins.LOGGER.error(e);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void receiveLayerList(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        try {
            int layerCount = packetByteBuf.readInt();
            OriginLayer[] layers = new OriginLayer[layerCount];
            for(int i = 0; i < layerCount; i++) {
                layers[i] = OriginLayer.read(packetByteBuf);
            }
            minecraftClient.execute(() -> {
                OriginLayers.clear();
                for(int i = 0; i < layerCount; i++) {
                    OriginLayers.add(layers[i]);
                }
                OriginDataLoadedCallback.EVENT.invoker().onDataLoaded(true);
            });
        } catch (Exception e) {
            Origins.LOGGER.error(e);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void receiveBadgeList(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        try {
            HashMap<Identifier, List<Badge>> badges = new HashMap<>();
            int count = packetByteBuf.readInt();
            for(int i = 0; i < count; i++) {
                Identifier powerId = packetByteBuf.readIdentifier();
                List<Badge> badgeList = new LinkedList<>();
                int badgeCount = packetByteBuf.readInt();
                for(int j = 0; j < badgeCount; j++) {
                    Badge badge = BadgeManager.REGISTRY.receiveDataObject(packetByteBuf);
                    badgeList.add(badge);
                }
                badges.put(powerId, badgeList);
            }
            minecraftClient.execute(() -> {
                BadgeManager.clear();
                for(Map.Entry<Identifier, List<Badge>> badgeEntry : badges.entrySet()) {
                    for(Badge badge : badgeEntry.getValue()) {
                        BadgeManager.putPowerBadge(badgeEntry.getKey(), badge);
                    }
                }
            });
        } catch (Exception e) {
            Origins.LOGGER.error(e);
        }
    }
}
