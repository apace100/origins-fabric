package io.github.apace100.origins.component;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.type.ModifyPlayerSpawnPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.origins.origin.*;
import io.github.apace100.origins.power.type.OriginsActionOnCallbackPowerType;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.*;

public interface OriginComponent extends AutoSyncedComponent, ServerTickingComponent {

	Map<OriginLayer, Origin> getOrigins();
	Origin getOrigin(OriginLayer layer);

	boolean hasSelectionInvulnerability();
	boolean isSelectingOrigin();
	boolean hasOrigin(OriginLayer layer);
	boolean hasAllOrigins();
	boolean hadOriginBefore();

	void selectingOrigin(boolean selectingOrigin);
	void removeLayer(OriginLayer layer);
	void setOrigin(OriginLayer layer, Origin origin);
	void sync();

	static void sync(PlayerEntity player) {
		ModComponents.ORIGIN.sync(player);
	}

	static void onChosen(PlayerEntity player, boolean hadOriginBefore) {

		if (!hadOriginBefore) {
			PowerHolderComponent.getPowerTypes(player, ModifyPlayerSpawnPowerType.class)
				.stream()
				.max(Comparator.comparing(ModifyPlayerSpawnPowerType::getPriority))
				.ifPresent(ModifyPlayerSpawnPowerType::teleportToModifiedSpawn);
		}

		PowerHolderComponent.withPowerTypes(player, OriginsActionOnCallbackPowerType.class, p -> true, p -> p.onChosen(hadOriginBefore));

	}

	static void partialOnChosen(PlayerEntity player, boolean hadOriginBefore, Origin origin) {

		PowerHolderComponent powerHolder = PowerHolderComponent.KEY.get(player);

		for (Power power : powerHolder.getPowersFromSource(origin.getId())) {

			PowerType powerType  = powerHolder.getPowerType(power);

			if (powerType instanceof ModifyPlayerSpawnPowerType mps && !hadOriginBefore) {
				mps.teleportToModifiedSpawn();
			}

			else if (powerType instanceof OriginsActionOnCallbackPowerType ocp) {
				ocp.onChosen(hadOriginBefore);
			}

		}

	}

	default boolean checkAutoChoosingLayers(PlayerEntity player, boolean includeDefaults) {

		List<OriginLayer> layers = new ArrayList<>();
		boolean choseOneAutomatically = false;

		OriginLayerManager.values()
			.stream()
			.filter(OriginLayer::isEnabled)
			.forEach(layers::add);

		Collections.sort(layers);
		for (OriginLayer layer : layers) {

			if (!layer.isEnabled() || hasOrigin(layer)) {
				continue;
			}

			if (includeDefaults && layer.hasDefaultOrigin()) {

				setOrigin(layer, OriginManager.get(layer.getDefaultOrigin()));
				choseOneAutomatically = true;

			} else if (layer.getOriginOptionCount(player) == 1 && layer.shouldAutoChoose()) {

				List<Origin> origins = layer.getOrigins(player)
					.stream()
					.map(OriginManager::get)
					.filter(Origin::isChoosable)
					.toList();

				if (!origins.isEmpty()) {

					setOrigin(layer, origins.getFirst());
					choseOneAutomatically = true;

				} else if (layer.isRandomAllowed() && !layer.getRandomOrigins(player).isEmpty()) {

					List<Identifier> randomOriginIds = layer.getRandomOrigins(player);
					int randomOriginIndex = player.getRandom().nextInt(randomOriginIds.size());

					setOrigin(layer, OriginManager.get(randomOriginIds.get(randomOriginIndex)));
					choseOneAutomatically = true;

				}

			}

		}

		return choseOneAutomatically;

	}

}
