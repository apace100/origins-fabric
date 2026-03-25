package io.github.apace100.origins.networking;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.packet.s2c.OriginsInstalledS2CPacket;
import io.github.apace100.origins.networking.packet.VersionHandshakePacket;
import io.github.apace100.origins.networking.packet.c2s.ChooseOriginC2SPacket;
import io.github.apace100.origins.networking.packet.c2s.ChooseRandomOriginC2SPacket;
import io.github.apace100.origins.networking.packet.s2c.ConfirmOriginS2CPacket;
import io.github.apace100.origins.networking.task.VersionHandshakeTask;
import io.github.apace100.origins.origin.*;
import io.github.apace100.origins.registry.ModComponents;
import joptsimple.internal.Strings;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.Arrays;
import java.util.Optional;

public class ModPacketsC2S {

    public static void register() {

        if (Origins.config.performVersionCheck) {
            ServerConfigurationConnectionEvents.CONFIGURE.register(ModPacketsC2S::sendHandshake);
            ServerConfigurationNetworking.registerGlobalReceiver(VersionHandshakePacket.PACKET_ID, ModPacketsC2S::receiveHandshakeReply);
        }

        ServerConfigurationConnectionEvents.CONFIGURE.register(ModPacketsC2S::sendOriginsInstallationStatus);

        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.registerReceiver(handler, ChooseOriginC2SPacket.PACKET_ID, ModPacketsC2S::onChooseOrigin);
            ServerPlayNetworking.registerReceiver(handler, ChooseRandomOriginC2SPacket.PACKET_ID, ModPacketsC2S::chooseRandomOrigin);
        });

    }

    private static void onChooseOrigin(ChooseOriginC2SPacket packet, ServerPlayNetworking.Context context) {

        ServerPlayerEntity player = context.player();
        OriginComponent originComponent = ModComponents.ORIGIN.get(player);

        OriginLayer layer = OriginLayerManager.getNullable(packet.layerId());
        Origin origin = OriginManager.getNullable(packet.originId());

        if (layer == null) {
            Origins.LOGGER.warn("Player {} tried to choose an origin for layer \"{}\", which doesn't exist!", player.getName().getString(), packet.layerId());
        }

        else if (origin == null) {
            Origins.LOGGER.warn("Player {} tried to choose origin \"{}\" for layer \"{}\", which doesn't exist!", player.getName().getString(), packet.originId(), packet.layerId());
        }

        else if (!originComponent.isSelectingOrigin()) {
            Origins.LOGGER.warn("Player {} tried to choose origin \"{}\" for layer \"{}\" while not actively selecting an origin!", player.getName().getString(), packet.originId(), packet.layerId());
        }

        else {

            if (originComponent.hasAllOrigins() && originComponent.hasOrigin(layer)) {
                Origins.LOGGER.warn("Player {} tried to choose origin \"{}\" for layer \"{}\" while having one already", player.getName().getString(), packet.originId(), packet.layerId());
            }

            else {

                if (!origin.isChoosable() || !layer.contains(origin, player)) {
                    Origins.LOGGER.warn("Player {} tried to choose origin \"{}\" from layer \"{}\", which cannot be chosen!", player.getName().getString(), packet.originId(), packet.layerId());
                }

                else {

                    boolean hadOriginBefore = originComponent.hadOriginBefore();
                    boolean hadAllOrigins = originComponent.hasAllOrigins();

                    originComponent.setOrigin(layer, origin);
                    originComponent.checkAutoChoosingLayers(player, false);

                    if (originComponent.hasAllOrigins() && !hadAllOrigins) {
                        OriginComponent.onChosen(player, hadOriginBefore);
                    }

                    Origins.LOGGER.info("Player {} chose origin \"{}\" for layer \"{}\"", player.getName().getString(), packet.originId(), packet.layerId());

                }

            }

            confirmOrigin(player, layer, originComponent.getOrigin(layer));

            originComponent.selectingOrigin(!originComponent.hasAllOrigins());
            originComponent.sync();

        }

    }

    private static void chooseRandomOrigin(ChooseRandomOriginC2SPacket packet, ServerPlayNetworking.Context context) {

        ServerPlayerEntity player = context.player();
        OriginComponent originComponent = ModComponents.ORIGIN.get(player);

        OriginLayer layer = OriginLayerManager.getNullable(packet.layerId());

        if (layer == null) {
            Origins.LOGGER.warn("Player {} tried to choose a random origin for layer \"{}\", which doesn't exist!", player.getName().getString(), packet.layerId());
        }

        else if (originComponent.hasAllOrigins() && originComponent.hasOrigin(layer)) {
            Origins.LOGGER.warn("Player {} tried to choose a random origin for layer \"{}\" while having one already", player.getName().getString(), packet.layerId());
        }

        else if (!layer.isRandomAllowed()) {
            Origins.LOGGER.warn("Player {} tried to choose a random origin for layer \"{}\", which is not allowed!", player.getName().getString(), packet.layerId());
        }

        else if (!originComponent.isSelectingOrigin()) {
            Origins.LOGGER.warn("Player {} tried to choose a random origin for layer \"{}\" while not actively selecting an origin!", player.getName().getString(), packet.layerId());
        }

        else {

            //  Random origin IDs are already validated whether they're in the origin manager, so no need to check here
            Optional<Origin> randomOrigin = Util
                .getRandomOrEmpty(layer.getRandomOrigins(player), player.getRandom())
                .map(OriginManager::get);

            if (randomOrigin.isEmpty()) {
                Origins.LOGGER.warn("Player {} tried to choose a random origin for layer \"{}\", which didn't have random origins specified!", player.getName().getString(), packet.layerId());
            }

            else {

                boolean hadOriginBefore = originComponent.hadOriginBefore();
                boolean hadAllOrigins = originComponent.hasAllOrigins();

                originComponent.setOrigin(layer, randomOrigin.get());
                originComponent.checkAutoChoosingLayers(player, false);

                if (originComponent.hasAllOrigins() && !hadAllOrigins) {
                    OriginComponent.onChosen(player, hadOriginBefore);
                }

                Origins.LOGGER.info("Player {} was randomly assigned the origin \"{}\" in layer \"{}\"", player.getName().getString(), randomOrigin.get().getId(), packet.layerId());

            }

            confirmOrigin(player, layer, originComponent.getOrigin(layer));

            originComponent.selectingOrigin(!originComponent.hasAllOrigins());
            originComponent.sync();

        }

    }

    private static void receiveHandshakeReply(VersionHandshakePacket packet, ServerConfigurationNetworking.Context context) {

        ServerConfigurationNetworkHandler handler = context.networkHandler();
        boolean mismatch = packet.semver().length != Origins.SEMVER.length;

        for (int i = 0; !mismatch && i < packet.semver().length - 1; i++) {
            mismatch = packet.semver()[i] != Origins.SEMVER[i];
        }

        if (mismatch) {
            handler.disconnect(Text.translatable("origins.gui.version_mismatch", Origins.VERSION, Strings.join(Arrays.stream(packet.semver()).mapToObj(String::valueOf).toList(), ".")));
        }

        else {
            handler.completeTask(VersionHandshakeTask.KEY);
        }

    }

    private static void sendOriginsInstallationStatus(ServerConfigurationNetworkHandler handler, MinecraftServer server) {
        handler.sendPacket(ServerConfigurationNetworking.createS2CPacket(OriginsInstalledS2CPacket.INSTANCE));
    }

    private static void sendHandshake(ServerConfigurationNetworkHandler handler, MinecraftServer server) {

        if (ServerConfigurationNetworking.canSend(handler, VersionHandshakePacket.PACKET_ID)) {
            handler.addTask(new VersionHandshakeTask(Origins.SEMVER));
        }

        else {
            handler.disconnect(Text.of("This server requires you to install the Origins mod (v " + Origins.VERSION + ") to play."));
        }

    }

    private static void confirmOrigin(ServerPlayerEntity player, OriginLayer layer, Origin origin) {
        ServerPlayNetworking.send(player, new ConfirmOriginS2CPacket(layer.getId(), origin.getId()));
    }

}
