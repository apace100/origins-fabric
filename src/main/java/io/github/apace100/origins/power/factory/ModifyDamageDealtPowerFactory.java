package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.ModifyDamageDealtPower;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.factory.condition.damage.DamageCondition;
import io.github.apace100.origins.util.SerializationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

public class ModifyDamageDealtPowerFactory extends PowerFactory<ModifyDamageDealtPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "modify_damage_dealt");

    private final List<List<DamageCondition>> damageConditions;
    private final EntityAttributeModifier modifier;

    public ModifyDamageDealtPowerFactory(List<List<DamageCondition>> damageConditions, EntityAttributeModifier modifier) {
        this.damageConditions = damageConditions;
        this.modifier = modifier;
    }

    @Override
    public ModifyDamageDealtPower apply(PowerType<ModifyDamageDealtPower> powerType, PlayerEntity playerEntity) {
        ModifyDamageDealtPower power = new ModifyDamageDealtPower(powerType, playerEntity, SerializationHelper.buildDamageConditionPredicate(damageConditions),
            modifier);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<ModifyDamageDealtPowerFactory> {

        @Override
        public void write(ModifyDamageDealtPowerFactory factory, PacketByteBuf buf) {
            SerializationHelper.writeDamageConditions(factory.damageConditions, buf);
            SerializationHelper.writeAttributeModifier(factory.modifier, buf);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public ModifyDamageDealtPowerFactory read(PacketByteBuf buf) {
            List<List<DamageCondition>> damageConditions = SerializationHelper.readDamageConditions(buf);
            EntityAttributeModifier modifier = SerializationHelper.readAttributeModifier(buf);
            ModifyDamageDealtPowerFactory factory = new ModifyDamageDealtPowerFactory(damageConditions, modifier);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public ModifyDamageDealtPowerFactory read(JsonObject json) {
            if(!json.has("modifier") || !json.get("modifier").isJsonObject()) {
                throw new JsonSyntaxException("ModifyDamageDealtPower json requires \"modifier\" json object");
            }
            EntityAttributeModifier mod = SerializationHelper.readAttributeModifier(json.getAsJsonObject("modifier"));
            List<List<DamageCondition>> conditions = new LinkedList<>();
            if(json.has("damage_condition")) {
                conditions = SerializationHelper.readDamageConditions(json.get("damage_condition"));
            }
            ModifyDamageDealtPowerFactory factory = new ModifyDamageDealtPowerFactory(conditions, mod);
            super.readConditions(factory, json);
            return factory;
        }
    }
}
