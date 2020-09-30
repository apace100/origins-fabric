package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.SwimSpeedPower;
import io.github.apace100.origins.util.SerializationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SwimSpeedPowerFactory extends PowerFactory<SwimSpeedPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "swim_speed");

    private final EntityAttributeModifier modifier;

    public SwimSpeedPowerFactory(EntityAttributeModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public SwimSpeedPower apply(PowerType<SwimSpeedPower> powerType, PlayerEntity playerEntity) {
        SwimSpeedPower power = new SwimSpeedPower(powerType, playerEntity, modifier);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<SwimSpeedPowerFactory> {

        @Override
        public void write(SwimSpeedPowerFactory factory, PacketByteBuf buf) {
            SerializationHelper.writeAttributeModifier(factory.modifier, buf);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public SwimSpeedPowerFactory read(PacketByteBuf buf) {
            SwimSpeedPowerFactory factory = new SwimSpeedPowerFactory(SerializationHelper.readAttributeModifier(buf));
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public SwimSpeedPowerFactory read(JsonObject json) {
            if(!json.has("modifier") || !json.get("modifier").isJsonObject()) {
                throw new JsonSyntaxException("SwimSpeedPower json requires \"modifier\" json object");
            }
            SwimSpeedPowerFactory factory = new SwimSpeedPowerFactory(SerializationHelper.readAttributeModifier(json.getAsJsonObject("modifier")));
            super.readConditions(factory, json);
            return factory;
        }
    }
}
