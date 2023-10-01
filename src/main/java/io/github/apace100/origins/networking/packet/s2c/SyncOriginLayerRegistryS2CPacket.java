package io.github.apace100.origins.networking.packet.s2c;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.OriginLayer;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public record SyncOriginLayerRegistryS2CPacket(Collection<OriginLayer> layers) implements FabricPacket {

    public static final PacketType<SyncOriginLayerRegistryS2CPacket> TYPE = PacketType.create(
        Origins.identifier("s2c/sync_origin_layer_registry"), SyncOriginLayerRegistryS2CPacket::read
    );

    private static SyncOriginLayerRegistryS2CPacket read(PacketByteBuf buffer) {

        List<OriginLayer> layers = new LinkedList<>();
        int size = buffer.readVarInt();

        for (int i = 0; i < size; i++) {
            layers.add(OriginLayer.read(buffer));
        }

        return new SyncOriginLayerRegistryS2CPacket(layers);

    }

    @Override
    public void write(PacketByteBuf buffer) {

        buffer.writeVarInt(layers.size());

        for (OriginLayer layer : layers) {
            layer.write(buffer);
        }

    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
