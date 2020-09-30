package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.ModifyJumpPower;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.util.SerializationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ModifyJumpPowerFactory extends PowerFactory<ModifyJumpPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "modify_jump");

    private final EntityAttributeModifier modifier;

    public ModifyJumpPowerFactory(EntityAttributeModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public ModifyJumpPower apply(PowerType<ModifyJumpPower> powerType, PlayerEntity playerEntity) {
        ModifyJumpPower power = new ModifyJumpPower(powerType, playerEntity, modifier);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<ModifyJumpPowerFactory> {

        @Override
        public void write(ModifyJumpPowerFactory factory, PacketByteBuf buf) {
            SerializationHelper.writeAttributeModifier(factory.modifier, buf);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public ModifyJumpPowerFactory read(PacketByteBuf buf) {
            ModifyJumpPowerFactory factory = new ModifyJumpPowerFactory(SerializationHelper.readAttributeModifier(buf));
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public ModifyJumpPowerFactory read(JsonObject json) {
            if(!json.has("modifier") || !json.get("modifier").isJsonObject()) {
                throw new JsonSyntaxException("ModifyJumpPower json requires \"modifier\" json object");
            }
            ModifyJumpPowerFactory factory = new ModifyJumpPowerFactory(SerializationHelper.readAttributeModifier(json.getAsJsonObject("modifier")));
            super.readConditions(factory, json);
            return factory;
        }
    }
}
