package io.github.apace100.origins.power.factory.condition.damage;

import io.github.apace100.origins.registry.ModRegistries;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class DamageConditionSerializers {

    public static void register() {
        register(IsFireCondition.SERIALIZER, new SimpleDamageConditionSerializer(IsFireCondition::new));
        register(NameCondition.SERIALIZER, new NameCondition.Serializer());
        register(AmountCondition.SERIALIZER, new AmountCondition.Serializer());
    }

    private static void register(Identifier identifier, DamageCondition.Serializer serializer) {
        Registry.register(ModRegistries.DAMAGE_CONDITION_SERIALIZER, identifier, serializer);
    }
}
