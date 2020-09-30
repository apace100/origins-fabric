package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.ExhaustOverTimePower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ExhaustOverTimePowerFactory extends PowerFactory<ExhaustOverTimePower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "exhaust");

    private final int exhaustInterval;
    private final float exhaustion;

    public ExhaustOverTimePowerFactory(int exhaustInterval, float exhaustion) {
        this.exhaustInterval = exhaustInterval;
        this.exhaustion = exhaustion;
    }

    @Override
    public ExhaustOverTimePower apply(PowerType<ExhaustOverTimePower> powerType, PlayerEntity playerEntity) {
        ExhaustOverTimePower power = new ExhaustOverTimePower(powerType, playerEntity, exhaustInterval, exhaustion);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<ExhaustOverTimePowerFactory> {

        @Override
        public void write(ExhaustOverTimePowerFactory factory, PacketByteBuf buf) {
            buf.writeInt(factory.exhaustInterval);
            buf.writeFloat(factory.exhaustion);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public ExhaustOverTimePowerFactory read(PacketByteBuf buf) {
            ExhaustOverTimePowerFactory factory = new ExhaustOverTimePowerFactory(buf.readInt(), buf.readFloat());
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public ExhaustOverTimePowerFactory read(JsonObject json) {
            if(!json.has("interval")) {
                throw new JsonSyntaxException("ExhaustOverTimePower json requires \"interval\" integer.");
            }
            int interval = JsonHelper.getInt(json, "interval");
            if(!json.has("exhaustion")) {
                throw new JsonSyntaxException("ExhaustOverTimePower json requires \"exhaustion\" float.");
            }
            float exhaust = JsonHelper.getFloat(json, "exhaustion");
            ExhaustOverTimePowerFactory factory = new ExhaustOverTimePowerFactory(interval, exhaust);
            super.readConditions(factory, json);
            return factory;
        }
    }
}
