package io.github.apace100.origins.networking;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.packet.VersionHandshakePacket;
import io.github.apace100.origins.networking.packet.c2s.ChooseOriginC2SPacket;
import io.github.apace100.origins.networking.packet.c2s.ChooseRandomOriginC2SPacket;
import io.github.apace100.origins.networking.packet.s2c.ConfirmOriginS2CPacket;
import io.github.apace100.origins.networking.task.VersionHandshakeTask;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ModPacketsC2S {

    public static void register() {

        if (Origins.config.performVersionCheck) {
            ServerConfigurationConnectionEvents.CONFIGURE.register(ModPacketsC2S::handshake);
            ServerConfigurationNetworking.registerGlobalReceiver(VersionHandshakePacket.TYPE, ModPacketsC2S::handleHandshakeReply);
        }

        ServerPlayNetworking.registerGlobalReceiver(ChooseOriginC2SPacket.TYPE, ModPacketsC2S::onChooseOrigin);
        ServerPlayNetworking.registerGlobalReceiver(ChooseRandomOriginC2SPacket.TYPE, ModPacketsC2S::chooseRandomOrigin);

    }

    private static void onChooseOrigin(ChooseOriginC2SPacket packet, ServerPlayerEntity player, PacketSender responseSender) {

        OriginComponent component = ModComponents.ORIGIN.get(player);
        OriginLayer layer = OriginLayers.getLayer(packet.layerId());

        if (component.hasAllOrigins() && component.hasOrigin(layer)) {
            Origins.LOGGER.warn("Player {} tried to choose origin for layer \"{}\" while having one already.", player.getName().getString(), packet.layerId());
            return;
        }

        Origin origin = OriginRegistry.get(packet.originId());
        if (!(origin.isChoosable() || layer.contains(origin, player))) {
            Origins.LOGGER.warn("Player {} tried to choose unchoosable origin \"{}\" from layer \"{}\"!", player.getName().getString(), packet.originId(), packet.layerId());
            component.setOrigin(layer, Origin.EMPTY);
        } else {

            boolean hadOriginBefore = component.hadOriginBefore();
            boolean hadAllOrigins = component.hasAllOrigins();

            component.setOrigin(layer, origin);
            component.checkAutoChoosingLayers(player, false);

            component.sync();

            if (component.hasAllOrigins() && !hadAllOrigins) {
                OriginComponent.onChosen(player, hadOriginBefore);
            }

            Origins.LOGGER.info("Player {} chose origin \"{}\" for layer \"{}\"", player.getName().getString(), packet.originId(), packet.layerId());

        }

        confirmOrigin(player, layer, component.getOrigin(layer));

        component.selectingOrigin(false);
        component.sync();

    }

    private static void chooseRandomOrigin(ChooseRandomOriginC2SPacket packet, ServerPlayerEntity player, PacketSender responseSender) {

        OriginComponent component = ModComponents.ORIGIN.get(player);
        OriginLayer layer = OriginLayers.getLayer(packet.layerId());

        if (component.hasAllOrigins() && component.hasOrigin(layer)) {
            Origins.LOGGER.warn("Player {} tried to choose origin for layer \"{}\" while having one already.", player.getName().getString(), packet.layerId());
            return;
        }

        List<Identifier> randomOriginIds = layer.getRandomOrigins(player);
        if (!layer.isRandomAllowed() || randomOriginIds.isEmpty()) {
            Origins.LOGGER.warn("Player {} tried to choose a random origin for layer \"{}\", which is not allowed!", player.getName().getString(), packet.layerId());
            component.setOrigin(layer, Origin.EMPTY);
        } else {

            Identifier randomOriginId = randomOriginIds.get(player.getRandom().nextInt(randomOriginIds.size()));
            Origin origin = OriginRegistry.get(randomOriginId);

            boolean hadOriginBefore = component.hadOriginBefore();
            boolean hadAllOrigins = component.hasAllOrigins();

            component.setOrigin(layer, origin);
            component.checkAutoChoosingLayers(player, false);

            component.sync();
            if (component.hasAllOrigins() && !hadAllOrigins) {
                OriginComponent.onChosen(player, hadOriginBefore);
            }

            Origins.LOGGER.info("Player {} was randomly assigned the following origin: {}", player.getName().getString(), randomOriginId);

        }

        confirmOrigin(player, layer, component.getOrigin(layer));

        component.selectingOrigin(false);
        component.sync();

    }

    private static void handleHandshakeReply(VersionHandshakePacket packet, ServerConfigurationNetworkHandler handler, PacketSender responseSender) {

        boolean mismatch = packet.semver().length != Origins.SEMVER.length;
        for (int i = 0; !mismatch && i < packet.semver().length - 1; i++) {

            if (packet.semver()[i] != Origins.SEMVER[i]) {
                mismatch = true;
                break;
            }

        }

        if (!mismatch) {
            handler.completeTask(VersionHandshakeTask.KEY);
            return;
        }

        StringBuilder semverString = new StringBuilder();
        String separator = "";

        for (int i : packet.semver()) {
            semverString.append(separator).append(i);
            separator = ".";
        }

        handler.disconnect(Text.translatable("origins.gui.version_mismatch", Origins.VERSION, semverString));

    }

    private static void handshake(ServerConfigurationNetworkHandler handler, MinecraftServer server) {

        if (ServerConfigurationNetworking.canSend(handler, VersionHandshakePacket.TYPE)) {
            handler.addTask(new VersionHandshakeTask(Origins.SEMVER));
            return;
        }

        handler.disconnect(Text.of("This server requires you to install the Origins mod (v " + Origins.VERSION + ") to play."));

    }

    private static void confirmOrigin(ServerPlayerEntity player, OriginLayer layer, Origin origin) {
        ServerPlayNetworking.send(player, new ConfirmOriginS2CPacket(layer.getIdentifier(), origin.getIdentifier()));
    }

}
