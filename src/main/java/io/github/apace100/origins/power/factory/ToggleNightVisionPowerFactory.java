package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.ToggleNightVisionPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ToggleNightVisionPowerFactory extends PowerFactory<ToggleNightVisionPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "toggle_night_vision");
    private final float strength;
    private final boolean activeByDefault;

    public ToggleNightVisionPowerFactory(float strength, boolean activeByDefault) {
        this.strength = strength;
        this.activeByDefault = activeByDefault;
    }

    @Override
    public ToggleNightVisionPower apply(PowerType<ToggleNightVisionPower> powerType, PlayerEntity playerEntity) {
        ToggleNightVisionPower power = new ToggleNightVisionPower(powerType, playerEntity, strength, activeByDefault);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<ToggleNightVisionPowerFactory> {

        @Override
        public void write(ToggleNightVisionPowerFactory factory, PacketByteBuf buf) {
            buf.writeFloat(factory.strength);
            buf.writeBoolean(factory.activeByDefault);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public ToggleNightVisionPowerFactory read(PacketByteBuf buf) {
            ToggleNightVisionPowerFactory factory = new ToggleNightVisionPowerFactory(buf.readFloat(), buf.readBoolean());
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public ToggleNightVisionPowerFactory read(JsonObject json) {
            ToggleNightVisionPowerFactory factory = new ToggleNightVisionPowerFactory(
                JsonHelper.getFloat(json, "strength", 1.0F),
                JsonHelper.getBoolean(json, "active_by_default", true));
            super.readConditions(factory, json);
            return factory;
        }
    }
}
