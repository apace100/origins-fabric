package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.NetherSpawnPower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class NetherSpawnPowerFactory extends PowerFactory<NetherSpawnPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "nether_spawn");

    @Override
    public NetherSpawnPower apply(PowerType<NetherSpawnPower> powerType, PlayerEntity playerEntity) {
        return new NetherSpawnPower(powerType, playerEntity);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<NetherSpawnPowerFactory> {

        @Override
        public void write(NetherSpawnPowerFactory factory, PacketByteBuf buf) { }

        @Environment(EnvType.CLIENT)
        @Override
        public NetherSpawnPowerFactory read(PacketByteBuf buf) {
            return new NetherSpawnPowerFactory();
        }

        @Override
        public NetherSpawnPowerFactory read(JsonObject json) {
            return new NetherSpawnPowerFactory();
        }
    }
}
