package io.github.apace100.origins.networking;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.packet.VersionHandshakePacket;
import io.github.apace100.origins.networking.packet.s2c.OriginsInstalledS2CPacket;
import io.github.apace100.origins.networking.packet.s2c.*;
import io.github.apace100.origins.origin.*;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import io.github.apace100.origins.screen.WaitForNextLayerScreen;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {

        ClientConfigurationConnectionEvents.START.register(ModPacketsS2C::resetOriginsInstallationStatus);

        ClientConfigurationNetworking.registerGlobalReceiver(VersionHandshakePacket.PACKET_ID, ModPacketsS2C::sendHandshakeReply);
        ClientConfigurationNetworking.registerGlobalReceiver(OriginsInstalledS2CPacket.PACKET_ID, ModPacketsS2C::receiveOriginsInstallationStatus);

        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(ConfirmOriginS2CPacket.PACKET_ID, ModPacketsS2C::receiveOriginConfirmation);
            ClientPlayNetworking.registerReceiver(OpenChooseOriginScreenS2CPacket.PACKET_ID, ModPacketsS2C::openOriginScreen);
            ClientPlayNetworking.registerReceiver(SyncOriginLayersS2CPacket.PACKET_ID, OriginLayerManager::receive);
            ClientPlayNetworking.registerReceiver(SyncOriginsS2CPacket.PACKET_ID, OriginManager::receive);
            ClientPlayNetworking.registerReceiver(SyncBadgesS2CPacket.PACKET_ID, BadgeManager::receive);
        }));

    }

    @Environment(EnvType.CLIENT)
    private static void receiveOriginConfirmation(ConfirmOriginS2CPacket packet, ClientPlayNetworking.Context context) {

        ClientPlayerEntity player = context.player();

        OriginLayer layer = OriginLayerManager.get(packet.layerId());
        Origin origin = OriginManager.get(packet.originId());

        OriginComponent component = ModComponents.ORIGIN.get(player);
        component.setOrigin(layer, origin);

        if (context.client().currentScreen instanceof WaitForNextLayerScreen nextLayerScreen) {
            nextLayerScreen.nextOrClose();
        }

    }

    @Environment(EnvType.CLIENT)
    private static void openOriginScreen(OpenChooseOriginScreenS2CPacket packet, ClientPlayNetworking.Context context) {

        List<OriginLayer> layers = new ObjectArrayList<>();
        OriginComponent component = ModComponents.ORIGIN.get(context.player());

        OriginLayerManager.values()
            .stream()
            .filter(OriginLayer::isEnabled)
            .filter(Predicate.not(component::hasOrigin))
            .forEach(layers::add);

        Collections.sort(layers);
        context.client().setScreen(new ChooseOriginScreen(layers, packet.showBackground()));

    }

    @Environment(EnvType.CLIENT)
    private static void sendHandshakeReply(VersionHandshakePacket packet, ClientConfigurationNetworking.Context context) {
        context.responseSender().sendPacket(new VersionHandshakePacket(Origins.SEMVER));
    }

    @Environment(EnvType.CLIENT)
    private static void receiveOriginsInstallationStatus(OriginsInstalledS2CPacket packet, ClientConfigurationNetworking.Context context) {
        context.client().submit(() -> OriginsClient.isServerRunningOrigins = true);
    }

    @Environment(EnvType.CLIENT)
    private static void resetOriginsInstallationStatus(ClientConfigurationNetworkHandler handler, MinecraftClient client) {
        client.submit(() -> OriginsClient.isServerRunningOrigins = false);
    }

}
