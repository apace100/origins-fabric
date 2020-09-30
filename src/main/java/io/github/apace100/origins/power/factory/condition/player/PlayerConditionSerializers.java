package io.github.apace100.origins.power.factory.condition.player;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PlayerConditionSerializers {

    public static void register() {
        register(OnFireCondition.SERIALIZER, new SimplePlayerConditionSerializer(OnFireCondition::new));
        register(SubmergedCondition.SERIALIZER, new SubmergedCondition.Serializer());
        register(EntityPredicateCondition.SERIALIZER, new EntityPredicateCondition.Serializer());
        register(PowerActiveCondition.SERIALIZER, new PowerActiveCondition.Serializer());
        register(IsFallFlyingCondition.SERIALIZER, new SimplePlayerConditionSerializer(IsFallFlyingCondition::new));
        register(IsDaytimeCondition.SERIALIZER, new SimplePlayerConditionSerializer(IsDaytimeCondition::new));
        register(StatusEffectCondition.SERIALIZER, new StatusEffectCondition.Serializer());
        register(IsSkyVisibleCondition.SERIALIZER, new SimplePlayerConditionSerializer(IsSkyVisibleCondition::new));
        register(IsInDaylightCondition.SERIALIZER, new SimplePlayerConditionSerializer(IsInDaylightCondition::new));
        register(BrightnessCondition.SERIALIZER, new BrightnessCondition.Serializer());
        register(IsInvisibleCondition.SERIALIZER, new SimplePlayerConditionSerializer(IsInvisibleCondition::new));
        register(IsSneakingCondition.SERIALIZER, new SimplePlayerConditionSerializer(IsSneakingCondition::new));
        register(IsSprintingCondition.SERIALIZER, new SimplePlayerConditionSerializer(IsSprintingCondition::new));
        register(BlockCollisionCondition.SERIALIZER, new BlockCollisionCondition.Serializer());
    }

    private static void register(String path, PlayerCondition.Serializer serializer) {
        register(new Identifier(Origins.MODID, path), serializer);
    }

    private static void register(Identifier identifier, PlayerCondition.Serializer serializer) {
        Registry.register(ModRegistries.PLAYER_CONDITION_SERIALIZER, identifier, serializer);
    }
}
