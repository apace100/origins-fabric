package io.github.apace100.origins.networking.task;

import io.github.apace100.origins.networking.packet.VersionHandshakePacket;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerConfigurationTask;

import java.util.function.Consumer;

public record VersionHandshakeTask(int[] semver) implements ServerPlayerConfigurationTask {

    public static final ServerPlayerConfigurationTask.Key KEY = new ServerPlayerConfigurationTask.Key("origins:handshake/version");

    @Override
    public void sendPacket(Consumer<Packet<?>> sender) {
        sender.accept(ServerConfigurationNetworking.createS2CPacket(new VersionHandshakePacket(semver)));
    }

    @Override
    public Key getKey() {
        return KEY;
    }

}
