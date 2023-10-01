package io.github.apace100.origins.networking.packet.s2c;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record SyncOriginRegistryS2CPacket(Map<Identifier, Origin> origins) implements FabricPacket {

    public static final PacketType<SyncOriginRegistryS2CPacket> TYPE = PacketType.create(
        Origins.identifier("s2c/sync_origin_registry"), SyncOriginRegistryS2CPacket::read
    );

    private static SyncOriginRegistryS2CPacket read(PacketByteBuf buffer) {

        int size = buffer.readVarInt();
        Map<Identifier, Origin> origins = new HashMap<>();

        for (int i = 0; i < size; i++) {

            Identifier originId = buffer.readIdentifier();
            Origin origin = Origin.createFromData(originId, Origin.DATA.read(buffer));

            origins.put(originId, origin);

        }

        return new SyncOriginRegistryS2CPacket(origins);

    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeMap(
            origins,
            PacketByteBuf::writeIdentifier,
            (vBuffer, origin) -> origin.write(vBuffer)
        );
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
