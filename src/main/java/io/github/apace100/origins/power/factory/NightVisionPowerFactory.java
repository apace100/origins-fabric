package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.NightVisionPower;
import io.github.apace100.origins.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class NightVisionPowerFactory extends PowerFactory<NightVisionPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "night_vision");
    private final float strength;

    public NightVisionPowerFactory(float strength) {
        this.strength = strength;
    }

    @Override
    public NightVisionPower apply(PowerType<NightVisionPower> powerType, PlayerEntity playerEntity) {
        NightVisionPower power = new NightVisionPower(powerType, playerEntity, strength);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<NightVisionPowerFactory> {

        @Override
        public void write(NightVisionPowerFactory factory, PacketByteBuf buf) {
            buf.writeFloat(factory.strength);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public NightVisionPowerFactory read(PacketByteBuf buf) {
            NightVisionPowerFactory factory = new NightVisionPowerFactory(buf.readFloat());
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public NightVisionPowerFactory read(JsonObject json) {
            NightVisionPowerFactory factory = new NightVisionPowerFactory(JsonHelper.getFloat(json, "strength", 1.0F));
            super.readConditions(factory, json);
            return factory;
        }
    }
}
