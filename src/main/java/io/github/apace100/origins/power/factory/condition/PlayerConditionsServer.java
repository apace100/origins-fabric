package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.mixin.ServerPlayerInteractionManagerAccessor;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.SerializableData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

public final class PlayerConditionsServer {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Origins.identifier("using_effective_tool"), new SerializableData(),
            (data, player) -> {

                if(player instanceof ServerPlayerEntity) {
                    ServerPlayerInteractionManagerAccessor interactionMngr = ((ServerPlayerInteractionManagerAccessor)((ServerPlayerEntity)player).interactionManager);
                    if(interactionMngr.getMining()) {
                        return player.isUsingEffectiveTool(player.world.getBlockState(interactionMngr.getMiningPos()));
                    }
                }
                return false;
            }));
    }

    private static void register(ConditionFactory<PlayerEntity> conditionFactory) {
        Registry.register(ModRegistries.PLAYER_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
