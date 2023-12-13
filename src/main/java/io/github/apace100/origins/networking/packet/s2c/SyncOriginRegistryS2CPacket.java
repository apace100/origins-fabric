package io.github.apace100.origins.networking.packet.s2c;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Map;

public record SyncOriginRegistryS2CPacket(Map<Identifier, SerializableData.Instance> origins) implements FabricPacket {

    public static final PacketType<SyncOriginRegistryS2CPacket> TYPE = PacketType.create(
        Origins.identifier("s2c/sync_origin_registry"), SyncOriginRegistryS2CPacket::read
    );

    private static SyncOriginRegistryS2CPacket read(PacketByteBuf buffer) {
        return new SyncOriginRegistryS2CPacket(buffer.readMap(PacketByteBuf::readIdentifier, Origin.DATA::read));
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeMap(origins, PacketByteBuf::writeIdentifier, Origin.DATA::write);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
