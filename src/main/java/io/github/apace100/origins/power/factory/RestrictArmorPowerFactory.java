package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.RestrictArmorPower;
import io.github.apace100.origins.power.factory.condition.item.ItemCondition;
import io.github.apace100.origins.util.SerializationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class RestrictArmorPowerFactory extends PowerFactory<RestrictArmorPower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "restrict_armor");

    private final List<List<ItemCondition>> headCondition;
    private final List<List<ItemCondition>> chestCondition;
    private final List<List<ItemCondition>> legsCondition;
    private final List<List<ItemCondition>> feetCondition;

    public RestrictArmorPowerFactory(List<List<ItemCondition>> headCondition, List<List<ItemCondition>> chestCondition, List<List<ItemCondition>> legsCondition, List<List<ItemCondition>> feetCondition) {
        this.headCondition = headCondition;
        this.chestCondition = chestCondition;
        this.legsCondition = legsCondition;
        this.feetCondition = feetCondition;
    }

    @Override
    public RestrictArmorPower apply(PowerType<RestrictArmorPower> powerType, PlayerEntity playerEntity) {
        HashMap<EquipmentSlot, Predicate<ItemStack>> map = new HashMap<>();
        map.put(EquipmentSlot.HEAD, SerializationHelper.buildItemConditionPredicate(headCondition));
        map.put(EquipmentSlot.CHEST, SerializationHelper.buildItemConditionPredicate(chestCondition));
        map.put(EquipmentSlot.LEGS, SerializationHelper.buildItemConditionPredicate(legsCondition));
        map.put(EquipmentSlot.FEET, SerializationHelper.buildItemConditionPredicate(feetCondition));
        RestrictArmorPower power = new RestrictArmorPower(powerType, playerEntity, map);
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<RestrictArmorPowerFactory> {

        @Override
        public void write(RestrictArmorPowerFactory factory, PacketByteBuf buf) {
            SerializationHelper.writeItemConditions(factory.headCondition, buf);
            SerializationHelper.writeItemConditions(factory.chestCondition, buf);
            SerializationHelper.writeItemConditions(factory.legsCondition, buf);
            SerializationHelper.writeItemConditions(factory.feetCondition, buf);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public RestrictArmorPowerFactory read(PacketByteBuf buf) {
            List<List<ItemCondition>> head = SerializationHelper.readItemConditions(buf);
            List<List<ItemCondition>> chest = SerializationHelper.readItemConditions(buf);
            List<List<ItemCondition>> legs = SerializationHelper.readItemConditions(buf);
            List<List<ItemCondition>> feet = SerializationHelper.readItemConditions(buf);
            RestrictArmorPowerFactory factory = new RestrictArmorPowerFactory(head, chest, legs, feet);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public RestrictArmorPowerFactory read(JsonObject json) {
            if(json.has("any")) {
                if(json.has("head") || json.has("chest") || json.has("legs") || json.has("feet")) {
                    throw new JsonSyntaxException("RestrictArmorPower json requires either \"any\" or more specific slots, not both.");
                }
                List<List<ItemCondition>> any = SerializationHelper.readItemConditions(json.get("any"));
                return new RestrictArmorPowerFactory(any, any, any, any);
            } else {
                if(!json.has("head") && !json.has("chest") && !json.has("legs") && !json.has("feet")) {
                    throw new JsonSyntaxException("RestrictArmorPower json requires either \"any\" or at least one more specific slots");
                }

                List<List<ItemCondition>> head = new LinkedList<>();
                if(json.has("head")) {
                    head = SerializationHelper.readItemConditions(json.get("head"));
                }


                List<List<ItemCondition>> chest = new LinkedList<>();
                if(json.has("chest")) {
                    chest = SerializationHelper.readItemConditions(json.get("chest"));
                }

                List<List<ItemCondition>> legs = new LinkedList<>();
                if(json.has("legs")) {
                    legs = SerializationHelper.readItemConditions(json.get("legs"));
                }

                List<List<ItemCondition>> feet = new LinkedList<>();
                if(json.has("feet")) {
                    feet = SerializationHelper.readItemConditions(json.get("feet"));
                }

                return new RestrictArmorPowerFactory(head, chest, legs, feet);
            }
        }
    }
}
