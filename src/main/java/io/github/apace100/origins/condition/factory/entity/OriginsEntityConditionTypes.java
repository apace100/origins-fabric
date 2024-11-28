package io.github.apace100.origins.condition.factory.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.condition.entity.type.OriginEntityConditionType;
import net.minecraft.registry.Registry;

public class OriginsEntityConditionTypes {

    public static final ConditionConfiguration<OriginEntityConditionType> ORIGIN = register(ConditionConfiguration.of(Origins.identifier("origin"), OriginEntityConditionType.DATA_FACTORY));

    public static void register() {

    }

    public static <T extends EntityConditionType> ConditionConfiguration<T> register(ConditionConfiguration<T> config) {

		//noinspection unchecked
		ConditionConfiguration<EntityConditionType> casted = (ConditionConfiguration<EntityConditionType>) config;
        Registry.register(ApoliRegistries.ENTITY_CONDITION_TYPE, casted.id(), casted);

        return config;

    }

}
