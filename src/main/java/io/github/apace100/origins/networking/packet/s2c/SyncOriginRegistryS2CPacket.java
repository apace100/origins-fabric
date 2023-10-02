package io.github.apace100.origins.networking.packet.s2c;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record SyncOriginRegistryS2CPacket(Map<Identifier, SerializableData.Instance> origins) implements FabricPacket {

    public static final PacketType<SyncOriginRegistryS2CPacket> TYPE = PacketType.create(
        Origins.identifier("s2c/sync_origin_registry"), SyncOriginRegistryS2CPacket::read
    );

    private static SyncOriginRegistryS2CPacket read(PacketByteBuf buffer) {

        Map<Identifier, SerializableData.Instance> origins = new HashMap<>();
        int size = buffer.readVarInt();

        for (int i = 0; i < size; i++) {

            Identifier originId = buffer.readIdentifier();
            SerializableData.Instance originData = Origin.DATA.read(buffer);

            if (!originId.equals(Origin.EMPTY.getIdentifier())) {
                origins.put(originId, originData);
            }

        }

        return new SyncOriginRegistryS2CPacket(origins);

    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeMap(
            origins,
            PacketByteBuf::writeIdentifier,
            Origin.DATA::write
        );
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
