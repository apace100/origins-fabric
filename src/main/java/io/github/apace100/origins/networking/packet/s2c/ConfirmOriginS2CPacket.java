package io.github.apace100.origins.networking.packet.s2c;

import io.github.apace100.origins.Origins;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record ConfirmOriginS2CPacket(Identifier layerId, Identifier originId) implements FabricPacket {

    public static final PacketType<ConfirmOriginS2CPacket> TYPE = PacketType.create(
        Origins.identifier("s2c/confirm_origin"), ConfirmOriginS2CPacket::read
    );

    private static ConfirmOriginS2CPacket read(PacketByteBuf buffer) {
        return new ConfirmOriginS2CPacket(buffer.readIdentifier(), buffer.readIdentifier());
    }

    @Override
    public void write(PacketByteBuf buffer) {
         buffer.writeIdentifier(layerId);
         buffer.writeIdentifier(originId);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
