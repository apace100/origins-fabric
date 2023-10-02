package io.github.apace100.origins.origin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypes;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.networking.packet.s2c.SyncOriginRegistryS2CPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class OriginManager extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {

	public static final Identifier PHASE = Origins.identifier("phase/origins");
	private static final Gson GSON = new GsonBuilder()
		.disableHtmlEscaping()
		.setPrettyPrinting()
		.create();

	public OriginManager() {
		super(GSON, "origins", ResourceType.SERVER_DATA);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(PowerTypes.PHASE, PHASE);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(PHASE, (player, joined) -> {

			Map<Identifier, SerializableData.Instance> origins = new HashMap<>();

			OriginRegistry.forEach((id, origin) -> origins.put(id, origin.toData()));
			ServerPlayNetworking.send(player, new SyncOriginRegistryS2CPacket(origins));

		});
	}

	@Override
	protected void apply(List<MultiJsonDataContainer> loader, ResourceManager manager, Profiler profiler) {

		OriginRegistry.reset();
		AtomicBoolean hasConfigChanged = new AtomicBoolean(false);

		loader.forEach(multiJsonDataContainer -> multiJsonDataContainer.forEach((packName, fileId, jsonElement) -> {

			try {

				Origin origin = Origin.fromJson(fileId, jsonElement.getAsJsonObject());
				int loadingPriority = origin.getLoadingPriority();

				if (!OriginRegistry.contains(fileId)) {
					OriginRegistry.register(fileId, origin);
				} else if (OriginRegistry.get(fileId).getLoadingPriority() < loadingPriority) {
					Origins.LOGGER.warn("Overriding origin \"{}\" (with prev. loading priority of {}) with a higher loading priority of {} from data pack [{}]!", fileId, OriginRegistry.get(fileId).getLoadingPriority(), loadingPriority, packName);
					OriginRegistry.update(fileId, origin);
				}

			} catch (Exception e) {
				Origins.LOGGER.error("There was a problem reading origin file \"{}\" (skipping): {}", fileId, e.getMessage());
			}

			if (!OriginRegistry.contains(fileId)) {
				return;
			}

			Origin origin = OriginRegistry.get(fileId);
			hasConfigChanged.set(hasConfigChanged.get() | Origins.config.addToConfig(origin));

			if (Origins.config.isOriginDisabled(fileId)) {
				OriginRegistry.remove(fileId);
				return;
			}

			List<PowerType<?>> powers = new LinkedList<>();
			origin.getPowerTypes().forEach(powers::add);

			for (PowerType<?> power : powers) {
				if (Origins.config.isPowerDisabled(fileId, power.getIdentifier())) {
					origin.removePowerType(power);
				}
			}

		}));

		Origins.LOGGER.info("Finished loading origins from data files. Registry contains {} origins.", OriginRegistry.size());
		if (hasConfigChanged.get()) {
			Origins.serializeConfig();
		}

	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(Origins.MODID, "origins");
	}

	@Override
	public Collection<Identifier> getFabricDependencies() {
		return Set.of(Apoli.identifier("powers"));
	}

}
