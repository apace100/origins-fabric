package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.ModifyExhaustionPower;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.util.SerializationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ModifyExhaustionPowerFactory extends PowerFactory<ModifyExhaustionPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "modify_exhaustion");

    private final EntityAttributeModifier modifier;

    public ModifyExhaustionPowerFactory(EntityAttributeModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public ModifyExhaustionPower apply(PowerType<ModifyExhaustionPower> powerType, PlayerEntity playerEntity) {
        ModifyExhaustionPower power = new ModifyExhaustionPower(powerType, playerEntity, modifier);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<ModifyExhaustionPowerFactory> {

        @Override
        public void write(ModifyExhaustionPowerFactory factory, PacketByteBuf buf) {
            SerializationHelper.writeAttributeModifier(factory.modifier, buf);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public ModifyExhaustionPowerFactory read(PacketByteBuf buf) {
            ModifyExhaustionPowerFactory factory = new ModifyExhaustionPowerFactory(SerializationHelper.readAttributeModifier(buf));
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public ModifyExhaustionPowerFactory read(JsonObject json) {
            if(!json.has("modifier") || !json.get("modifier").isJsonObject()) {
                throw new JsonSyntaxException("ModifyExhaustionPower json requires \"modifier\" json object");
            }
            ModifyExhaustionPowerFactory factory = new ModifyExhaustionPowerFactory(SerializationHelper.readAttributeModifier(json.getAsJsonObject("modifier")));
            super.readConditions(factory, json);
            return factory;
        }
    }
}
