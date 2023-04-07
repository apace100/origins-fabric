package io.github.apace100.origins.component;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyPlayerSpawnPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.OriginsCallbackPower;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public interface OriginComponent extends AutoSyncedComponent {

	boolean hasOrigin(OriginLayer layer);
	boolean hasAllOrigins();

	HashMap<OriginLayer, Origin> getOrigins();
	Origin getOrigin(OriginLayer layer);

	boolean hadOriginBefore();

	void setOrigin(OriginLayer layer, Origin origin);

	void sync();

	@Deprecated(forRemoval = true)
	void onPowersRead();

	static void sync(PlayerEntity player) {
		ModComponents.ORIGIN.sync(player);
		PowerHolderComponent.KEY.sync(player);
	}

	static void onChosen(PlayerEntity player, boolean hadOriginBefore) {
		if(!hadOriginBefore) {
			PowerHolderComponent.getPowers(player, ModifyPlayerSpawnPower.class).forEach(ModifyPlayerSpawnPower::teleportToModifiedSpawn);
		}
		PowerHolderComponent.getPowers(player, OriginsCallbackPower.class).forEach(p -> p.onChosen(hadOriginBefore));
	}

	static void partialOnChosen(PlayerEntity player, boolean hadOriginBefore, Origin origin) {
		PowerHolderComponent powerHolder = PowerHolderComponent.KEY.get(player);
		for(PowerType<?> powerType : powerHolder.getPowersFromSource(origin.getIdentifier())) {
			Power p = powerHolder.getPower(powerType);
			if(p instanceof ModifyPlayerSpawnPower && !hadOriginBefore) {
				((ModifyPlayerSpawnPower)p).teleportToModifiedSpawn();
			} else
			if(p instanceof OriginsCallbackPower) {
				((OriginsCallbackPower)p).onChosen(hadOriginBefore);
			}
		}
	}

	default boolean checkAutoChoosingLayers(PlayerEntity player, boolean includeDefaults) {
		boolean choseOneAutomatically = false;
		ArrayList<OriginLayer> layers = new ArrayList<>();
		for(OriginLayer layer : OriginLayers.getLayers()) {
			if(layer.isEnabled()) {
				layers.add(layer);
			}
		}
		Collections.sort(layers);
		for(OriginLayer layer : layers) {
			boolean shouldContinue = false;
			if (layer.isEnabled() && !hasOrigin(layer)) {
				if (includeDefaults && layer.hasDefaultOrigin()) {
					setOrigin(layer, OriginRegistry.get(layer.getDefaultOrigin()));
					choseOneAutomatically = true;
					shouldContinue = true;
				} else if (layer.getOriginOptionCount(player) == 1 && layer.shouldAutoChoose()) {
					List<Origin> origins = layer.getOrigins(player).stream().map(OriginRegistry::get).filter(Origin::isChoosable).collect(Collectors.toList());
					if (origins.size() == 0) {
						List<Identifier> randomOrigins = layer.getRandomOrigins(player);
						setOrigin(layer, OriginRegistry.get(randomOrigins.get(player.getRandom().nextInt(randomOrigins.size()))));
					} else {
						setOrigin(layer, origins.get(0));
					}
					choseOneAutomatically = true;
					shouldContinue = true;
				} else if(layer.getOriginOptionCount(player) == 0) {
					shouldContinue = true;
				}
			} else {
				shouldContinue = true;
			}
			if(!shouldContinue) {
				break;
			}
		}
		return choseOneAutomatically;
	}
}
