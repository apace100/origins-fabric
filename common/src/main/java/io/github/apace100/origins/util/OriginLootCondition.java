package io.github.apace100.origins.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModLoot;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OriginLootCondition implements LootCondition {
    private final Identifier origin;

    private OriginLootCondition(Identifier origin) {
        this.origin = origin;
    }

    public LootConditionType getType() {
        return ModLoot.ORIGIN_LOOT_CONDITION;
    }

    public boolean test(LootContext lootContext) {
        Optional<OriginComponent> optional = ModComponents.ORIGIN.maybeGet(lootContext.get(LootContextParameters.THIS_ENTITY));
        if(optional.isPresent()){
            OriginComponent component = optional.get();
            HashMap<OriginLayer, Origin> map = component.getOrigins();
            boolean matches = false;
            for (Map.Entry<OriginLayer, Origin> entry: map.entrySet()) {
                if(entry.getValue().getIdentifier().equals(origin)) {
                    matches = true;
                    break;
                }
            }
            return matches;
        }
        return false;
    }

    public static LootCondition.Builder builder(String originId) {
        return builder(new Identifier(originId));
    }

    public static LootCondition.Builder builder(Identifier origin) {
        return () -> {
            return new OriginLootCondition(origin);
        };
    }

    public static class Serializer implements JsonSerializer<OriginLootCondition> {
        public void toJson(JsonObject jsonObject, OriginLootCondition originLootCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("origin", originLootCondition.origin.toString());
        }

        public OriginLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new OriginLootCondition(new Identifier(JsonHelper.getString(jsonObject, "origin")));
        }
    }
}
