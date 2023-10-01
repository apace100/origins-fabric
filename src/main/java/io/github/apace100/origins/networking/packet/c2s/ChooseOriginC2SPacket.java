package io.github.apace100.origins.networking.packet.c2s;

import io.github.apace100.origins.Origins;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record ChooseOriginC2SPacket(Identifier layerId, Identifier originId) implements FabricPacket {

    public static final PacketType<ChooseOriginC2SPacket> TYPE = PacketType.create(
        Origins.identifier("c2s/choose_origin"), ChooseOriginC2SPacket::read
    );

    private static ChooseOriginC2SPacket read(PacketByteBuf buffer) {
        return new ChooseOriginC2SPacket(buffer.readIdentifier(), buffer.readIdentifier());
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
