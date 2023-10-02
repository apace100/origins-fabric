package io.github.apace100.origins.networking.packet.s2c;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.OriginLayer;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record SyncOriginLayerRegistryS2CPacket(Map<Identifier, OriginLayer> layers) implements FabricPacket {

    public static final PacketType<SyncOriginLayerRegistryS2CPacket> TYPE = PacketType.create(
        Origins.identifier("s2c/sync_origin_layer_registry"), SyncOriginLayerRegistryS2CPacket::read
    );

    private static SyncOriginLayerRegistryS2CPacket read(PacketByteBuf buffer) {

        Map<Identifier, OriginLayer> layers = new HashMap<>();
        int layersSize = buffer.readVarInt();

        for (int i = 0; i < layersSize; i++) {

            Identifier layerId = buffer.readIdentifier();
            OriginLayer layer = OriginLayer.read(buffer);

            layers.put(layerId, layer);

        }

        return new SyncOriginLayerRegistryS2CPacket(layers);

    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeMap(
            layers,
            PacketByteBuf::writeIdentifier,
            (valueBuffer, layer) -> layer.write(valueBuffer)
        );
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
