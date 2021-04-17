package io.github.apace100.origins.registry.fabric;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.FabricPlayerOriginComponent;
import io.github.apace100.origins.component.OriginComponent;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class ModComponentsArchitecturyImpl implements EntityComponentInitializer {

	public static final ComponentKey<FabricPlayerOriginComponent> ORIGIN;

	static {
		ORIGIN = ComponentRegistry.getOrCreate(Origins.identifier("origin"), FabricPlayerOriginComponent.class);
	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(ORIGIN, FabricPlayerOriginComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
	}

	public static OriginComponent getOriginComponent(Entity player) {
		return ORIGIN.get(player);
	}

	public static Optional<OriginComponent> maybeGetOriginComponent(Entity player) {
		return ORIGIN.maybeGet(player).map(x -> x);
	}

	public static void syncOriginComponent(Entity player) {
		ORIGIN.sync(player);
	}

	public static void syncWith(ServerPlayerEntity player, Entity provider) {
		ORIGIN.syncWith(player, ComponentProvider.fromEntity(provider));
	}
}
