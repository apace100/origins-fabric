package io.github.apace100.origins.power.factory.condition.item;

import io.github.apace100.origins.registry.ModRegistries;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemConditionSerializers {

    public static void register() {
        register(IsFoodCondition.SERIALIZER, new SimpleItemConditionSerializer(IsFoodCondition::new));
        register(IngredientCondition.SERIALIZER, new IngredientCondition.Serializer());
        register(ArmorValueCondition.SERIALIZER, new ArmorValueCondition.Serializer());
    }

    private static void register(Identifier identifier, ItemCondition.Serializer serializer) {
        Registry.register(ModRegistries.ITEM_CONDITION_SERIALIZER, identifier, serializer);
    }
}
