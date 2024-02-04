package io.github.apace100.origins.power;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.origins.power.condition.entity.OriginCondition;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;

public class OriginsEntityConditions {

    public static void register() {
        register(OriginCondition.getFactory());
    }

    private static void register(ConditionFactory<Entity> conditionFactory) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }

}
