package io.github.apace100.origins.power.factory.condition.block;

import io.github.apace100.origins.registry.ModRegistries;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockConditionSerializers {

    public static void register() {
        register(IsInTagCondition.SERIALIZER, new IsInTagCondition.Serializer());
        register(HeightCondition.SERIALIZER, new HeightCondition.Serializer());
        register(IsBlockCondition.SERIALIZER, new IsBlockCondition.Serializer());
        register(AdjacentCondition.SERIALIZER, new AdjacentCondition.Serializer());
    }

    private static void register(Identifier identifier, BlockCondition.Serializer serializer) {
        Registry.register(ModRegistries.BLOCK_CONDITION_SERIALIZER, identifier, serializer);
    }
}
