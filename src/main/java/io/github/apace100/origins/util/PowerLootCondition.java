package io.github.apace100.origins.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PowerTypeReference;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModLoot;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

import java.util.Optional;

public class PowerLootCondition implements LootCondition {
    private final PowerType<?> powerType;

    private PowerLootCondition(Identifier powerId) {
        this(new PowerTypeReference<>(powerId));
    }

    private PowerLootCondition(PowerType<?> powerType) {
        this.powerType = powerType;
    }

    public LootConditionType getType() {
        return ModLoot.POWER_LOOT_CONDITION;
    }

    public boolean test(LootContext lootContext) {
        Optional<OriginComponent> optional = ModComponents.ORIGIN.maybeGet(lootContext.get(LootContextParameters.THIS_ENTITY));
        if(optional.isPresent()){
            OriginComponent component = optional.get();
            return component.hasPower(powerType);
        }
        return false;
    }

    public static Builder builder(String powerId) {
        return builder(new Identifier(powerId));
    }

    public static Builder builder(Identifier powerId) {
        return builder(new PowerTypeReference<>(powerId));
    }

    public static Builder builder(PowerType<?> powerType) {
        return () -> {
            return new PowerLootCondition(powerType);
        };
    }

    public static class Serializer implements JsonSerializer<PowerLootCondition> {
        public void toJson(JsonObject jsonObject, PowerLootCondition originLootCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("power", originLootCondition.powerType.getIdentifier().toString());
        }

        public PowerLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new PowerLootCondition(new Identifier(JsonHelper.getString(jsonObject, "power")));
        }
    }
}
