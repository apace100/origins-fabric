package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.ModifyBreakSpeedPower;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.factory.condition.block.BlockCondition;
import io.github.apace100.origins.util.SerializationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModifyBreakSpeedPowerFactory extends PowerFactory<ModifyBreakSpeedPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "modify_break_speed");

    private final List<List<BlockCondition>> blockConditions;
    private final EntityAttributeModifier modifier;

    public ModifyBreakSpeedPowerFactory(List<List<BlockCondition>> blockConditions, EntityAttributeModifier modifier) {
        this.blockConditions = blockConditions;
        this.modifier = modifier;
    }

    @Override
    public ModifyBreakSpeedPower apply(PowerType<ModifyBreakSpeedPower> powerType, PlayerEntity playerEntity) {
        ModifyBreakSpeedPower power = new ModifyBreakSpeedPower(powerType, playerEntity, SerializationHelper.buildBlockConditionPredicate(blockConditions), modifier);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<ModifyBreakSpeedPowerFactory> {

        @Override
        public void write(ModifyBreakSpeedPowerFactory factory, PacketByteBuf buf) {
            SerializationHelper.writeBlockConditions(factory.blockConditions, buf);
            SerializationHelper.writeAttributeModifier(factory.modifier, buf);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public ModifyBreakSpeedPowerFactory read(PacketByteBuf buf) {
            List<List<BlockCondition>> conditions = SerializationHelper.readBlockConditions(buf);
            EntityAttributeModifier modifier = SerializationHelper.readAttributeModifier(buf);
            ModifyBreakSpeedPowerFactory factory = new ModifyBreakSpeedPowerFactory(conditions, modifier);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public ModifyBreakSpeedPowerFactory read(JsonObject json) {
            List<List<BlockCondition>> conditions = null;
            if(json.has("block_condition")) {
                conditions = SerializationHelper.readBlockConditions(json.get("block_condition"));
            }
            if(!json.has("modifier") || !json.get("modifier").isJsonObject()) {
                throw new JsonSyntaxException("ModifyBreakSpeedPower json requires \"modifier\" json object");
            }
            EntityAttributeModifier modifier = SerializationHelper.readAttributeModifier(json.getAsJsonObject("modifier"));
            ModifyBreakSpeedPowerFactory factory = new ModifyBreakSpeedPowerFactory(conditions, modifier);
            super.readConditions(factory, json);
            return factory;
        }
    }
}
