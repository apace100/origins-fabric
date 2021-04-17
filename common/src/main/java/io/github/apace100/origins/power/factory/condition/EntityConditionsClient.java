package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistriesArchitectury;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Identifier;

import java.util.Map;

public final class EntityConditionsClient {

    @SuppressWarnings("unchecked")
    @Environment(EnvType.CLIENT)
    public static void register() {
        register(new ConditionFactory<>(Origins.identifier("using_effective_tool"), new SerializableData(),
            (data, entity) -> {
                if(entity instanceof ServerPlayerEntity) {
                    ServerPlayerInteractionManager interactionMngr = ((ServerPlayerEntity) entity).interactionManager;
                    if(interactionMngr.mining) {
                        return ((PlayerEntity)entity).isUsingEffectiveTool(entity.world.getBlockState(interactionMngr.miningPos));
                    }
                } else
                if(entity instanceof ClientPlayerEntity) {
                    ClientPlayerInteractionManager interactionMngr = MinecraftClient.getInstance().interactionManager;
                    if(interactionMngr.isBreakingBlock()) {
                        return ((PlayerEntity)entity).isUsingEffectiveTool(entity.world.getBlockState(interactionMngr.currentBreakingPos));
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Origins.identifier("gamemode"), new SerializableData()
            .add("gamemode", SerializableDataType.STRING), (data, entity) -> {
            if(entity instanceof ServerPlayerEntity) {
                ServerPlayerInteractionManager interactionMngr = ((ServerPlayerEntity) entity).interactionManager;
                return interactionMngr.getGameMode().getName().equals(data.getString("gamemode"));
            } else
            if(entity instanceof ClientPlayerEntity) {
                return MinecraftClient.getInstance().interactionManager.getCurrentGameMode().getName().equals(data.getString("gamemode"));
            }
            return false;
        }));
        register(new ConditionFactory<>(Origins.identifier("advancement"), new SerializableData()
            .add("advancement", SerializableDataType.IDENTIFIER), (data, entity) -> {
            Identifier id = data.getId("advancement");
            if(entity instanceof ServerPlayerEntity) {
                Advancement advancement = entity.getServer().getAdvancementLoader().get(id);
                if(advancement == null) {
                    Origins.LOGGER.warn("Advancement \"" + id + "\" did not exist, but was referenced in an \"origins:advancement\" condition.");
                } else {
                    return ((ServerPlayerEntity)entity).getAdvancementTracker().getProgress(advancement).isDone();
                }
            } else
            if(entity instanceof ClientPlayerEntity) {
                ClientAdvancementManager advancementManager = MinecraftClient.getInstance().getNetworkHandler().getAdvancementHandler();
                Advancement advancement = advancementManager.getManager().get(id);
                if(advancement != null) {
                    Map<Advancement, AdvancementProgress> progressMap = advancementManager.advancementProgresses;
                    if(progressMap.containsKey(advancement)) {
                        return progressMap.get(advancement).isDone();
                    }
                }
                // We don't want to print an error here if the advancement does not exist,
                // because on the client-side the advancement could just not have been received from the server.
            }
            return false;
        }));
    }

    private static void register(ConditionFactory<LivingEntity> conditionFactory) {
        ModRegistriesArchitectury.ENTITY_CONDITION.registerSupplied(conditionFactory.getSerializerId(), () -> conditionFactory);
    }
}
