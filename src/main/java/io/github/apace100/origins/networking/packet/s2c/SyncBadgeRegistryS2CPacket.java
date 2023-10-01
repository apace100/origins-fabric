package io.github.apace100.origins.networking.packet.s2c;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.badge.BadgeManager;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public record SyncBadgeRegistryS2CPacket(Map<Identifier, List<Badge>> badges) implements FabricPacket {

    public static final PacketType<SyncBadgeRegistryS2CPacket> TYPE = PacketType.create(
        Origins.identifier("s2c/sync_badge_registry"), SyncBadgeRegistryS2CPacket::read
    );

    private static SyncBadgeRegistryS2CPacket read(PacketByteBuf buffer) {

        Map<Identifier, List<Badge>> badges = buffer.readMap(
            PacketByteBuf::readIdentifier,
            valueBuffer -> {

                List<Badge> badgeList = new LinkedList<>();
                int size = valueBuffer.readVarInt();

                for (int i = 0; i < size; i++) {
                    badgeList.add(BadgeManager.REGISTRY.receiveDataObject(valueBuffer));
                }

                return badgeList;

            }
        );

        return new SyncBadgeRegistryS2CPacket(badges);

    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeMap(
            badges,
            PacketByteBuf::writeIdentifier,
            (valueBuffer, badges) -> {

                valueBuffer.writeVarInt(badges.size());
                for (Badge badge : badges) {
                    badge.writeBuf(valueBuffer);
                }

            }
        );
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
