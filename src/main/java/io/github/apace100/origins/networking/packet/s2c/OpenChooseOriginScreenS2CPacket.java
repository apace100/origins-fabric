package io.github.apace100.origins.networking.packet.s2c;

import io.github.apace100.origins.Origins;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record OpenChooseOriginScreenS2CPacket(boolean showBackground) implements FabricPacket {

    public static final PacketType<OpenChooseOriginScreenS2CPacket> TYPE = PacketType.create(
        Origins.identifier("s2c/open_origin_screen"), OpenChooseOriginScreenS2CPacket::read
    );

    private static OpenChooseOriginScreenS2CPacket read(PacketByteBuf buffer) {
        return new OpenChooseOriginScreenS2CPacket(buffer.readBoolean());
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeBoolean(showBackground);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
