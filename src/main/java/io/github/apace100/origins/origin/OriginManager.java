package io.github.apace100.origins.origin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.packet.s2c.OpenOriginScreenS2CPacket;
import io.github.apace100.origins.networking.packet.s2c.SyncOriginLayerRegistryS2CPacket;
import io.github.apace100.origins.networking.packet.s2c.SyncOriginRegistryS2CPacket;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class OriginManager extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {
	
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	public OriginManager() {
		super(GSON, "origins", ResourceType.SERVER_DATA);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {

			OriginComponent component = ModComponents.ORIGIN.get(player);

			List<OriginLayer> originLayers = new LinkedList<>();
			Map<Identifier, Origin> origins = new HashMap<>();

			OriginRegistry.forEach(origins::put);
			SyncOriginRegistryS2CPacket syncOriginRegistryPacket = new SyncOriginRegistryS2CPacket(origins);

			for (OriginLayer layer : OriginLayers.getLayers()) {

				originLayers.add(layer);

				if (layer.isEnabled() && !component.hasOrigin(layer)) {
					component.setOrigin(layer, Origin.EMPTY);
				}

			}

			SyncOriginLayerRegistryS2CPacket syncOriginLayerRegistryPacket = new SyncOriginLayerRegistryS2CPacket(originLayers);

			ServerPlayNetworking.send(player, syncOriginRegistryPacket);
			ServerPlayNetworking.send(player, syncOriginLayerRegistryPacket);

			if (joined) {

				List<ServerPlayerEntity> players = player.getServerWorld().getServer().getPlayerManager().getPlayerList();
				players.remove(player);

				players.forEach(otherPlayer -> ModComponents.ORIGIN.syncWith(otherPlayer, (ComponentProvider) player));

			}

			postLoad(player, joined);

		});
	}

	private void postLoad(ServerPlayerEntity player, boolean init) {

		OriginComponent component = ModComponents.ORIGIN.get(player);
		if (component.hasAllOrigins()) {
			return;
		}

		if (component.checkAutoChoosingLayers(player, true)) {
			component.sync();
		}

		if (init) {

			OriginComponent.sync(player);

			if (component.hasAllOrigins()) {
				OriginComponent.onChosen(player, false);
			} else {
				ServerPlayNetworking.send(player, new OpenOriginScreenS2CPacket(true));
			}

		}

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
