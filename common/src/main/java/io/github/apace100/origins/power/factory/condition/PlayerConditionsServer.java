package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.SerializableData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.registry.Registry;

public final class PlayerConditionsServer {

    public static void register() {
        register(new ConditionFactory<>(Origins.identifier("using_effective_tool"), new SerializableData(),
            (data, player) -> {

                if(player instanceof ServerPlayerEntity) {
                    ServerPlayerInteractionManager interactionMngr = ((ServerPlayerEntity)player).interactionManager;
                    if(interactionMngr.mining) {
                        return player.isUsingEffectiveTool(player.world.getBlockState(interactionMngr.miningPos));
                    }
                }
                return false;
            }));
    }

    private static void register(ConditionFactory<PlayerEntity> conditionFactory) {
        ModRegistries.PLAYER_CONDITION.register(conditionFactory.getSerializerId(), () -> conditionFactory);
    }
}
