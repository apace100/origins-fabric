package io.github.apace100.origins.networking.packet.c2s;

import io.github.apace100.origins.Origins;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record ChooseRandomOriginC2SPacket(Identifier layerId) implements FabricPacket {

    public static final PacketType<ChooseRandomOriginC2SPacket> TYPE = PacketType.create(
        Origins.identifier("c2s/choose_random_origin"), ChooseRandomOriginC2SPacket::read
    );

    private static ChooseRandomOriginC2SPacket read(PacketByteBuf buffer) {
        return new ChooseRandomOriginC2SPacket(buffer.readIdentifier());
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeIdentifier(layerId);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
