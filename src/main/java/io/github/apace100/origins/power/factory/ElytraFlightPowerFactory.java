package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.ElytraFlightPower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ElytraFlightPowerFactory extends PowerFactory<ElytraFlightPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "elytra_flight");

    @Override
    public ElytraFlightPower apply(PowerType<ElytraFlightPower> powerType, PlayerEntity playerEntity) {
        return new ElytraFlightPower(powerType, playerEntity);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<ElytraFlightPowerFactory> {

        @Override
        public void write(ElytraFlightPowerFactory factory, PacketByteBuf buf) { }

        @Environment(EnvType.CLIENT)
        @Override
        public ElytraFlightPowerFactory read(PacketByteBuf buf) {
            return new ElytraFlightPowerFactory();
        }

        @Override
        public ElytraFlightPowerFactory read(JsonObject json) {
            return new ElytraFlightPowerFactory();
        }
    }
}
