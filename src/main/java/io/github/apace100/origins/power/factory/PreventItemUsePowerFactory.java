package io.github.apace100.origins.power.factory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PreventItemUsePower;
import io.github.apace100.origins.power.factory.condition.item.ItemCondition;
import io.github.apace100.origins.util.SerializationHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;

public class PreventItemUsePowerFactory extends PowerFactory<PreventItemUsePower> {

    private static final Identifier SERIALIZER = new Identifier(Origins.MODID, "prevent_item_use");

    private final List<List<ItemCondition>> itemConditions;

    public PreventItemUsePowerFactory(List<List<ItemCondition>> itemConditions) {
        this.itemConditions = itemConditions;
    }

    @Override
    public PreventItemUsePower apply(PowerType<PreventItemUsePower> powerType, PlayerEntity playerEntity) {
        PreventItemUsePower power = new PreventItemUsePower(powerType, playerEntity, SerializationHelper.buildItemConditionPredicate(itemConditions));
        super.addConditions(power);
        return power;
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }

    public static class Serializer extends PowerFactory.Serializer<PreventItemUsePowerFactory> {

        @Override
        public void write(PreventItemUsePowerFactory factory, PacketByteBuf buf) {
            SerializationHelper.writeItemConditions(factory.itemConditions, buf);
            super.writeConditions(factory, buf);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public PreventItemUsePowerFactory read(PacketByteBuf buf) {
            List<List<ItemCondition>> conditions = SerializationHelper.readItemConditions(buf);
            PreventItemUsePowerFactory factory = new PreventItemUsePowerFactory(conditions);
            super.readConditions(factory, buf);
            return factory;
        }

        @Override
        public PreventItemUsePowerFactory read(JsonObject json) {
            if(!json.has("item_condition")) {
                throw new JsonSyntaxException("PreventItemUsePower json requires \"item_condition\".");
            }
            List<List<ItemCondition>> conditions = SerializationHelper.readItemConditions(json.get("item_condition"));
            PreventItemUsePowerFactory factory = new PreventItemUsePowerFactory(conditions);
            super.readConditions(factory, json);
            return factory;
        }
    }
}
