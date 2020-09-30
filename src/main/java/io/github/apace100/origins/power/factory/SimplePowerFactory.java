package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SimplePowerFactory extends PowerFactory<Power> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "simple");

    @Override
    public Power apply(PowerType<Power> powerType, PlayerEntity playerEntity) {
        Power power = new Power(powerType, playerEntity);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<SimplePowerFactory> {

        @Override
        public void write(SimplePowerFactory factory, PacketByteBuf buf) {
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public SimplePowerFactory read(PacketByteBuf buf) {
            SimplePowerFactory factory = new SimplePowerFactory();
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public SimplePowerFactory read(JsonObject json) {
            SimplePowerFactory factory = new SimplePowerFactory();
            super.readConditions(factory, json);
            return factory;
        }
    }
}
