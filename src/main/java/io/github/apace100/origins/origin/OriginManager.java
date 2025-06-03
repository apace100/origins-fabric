package io.github.apace100.origins.origin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.calio.CalioServer;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.networking.packet.s2c.SyncOriginsS2CPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class OriginManager extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {

	public static final Set<Identifier> DEPENDENCIES = Util.make(new HashSet<>(), set -> set.add(PowerManager.ID));
	public static final Identifier ID = Origins.identifier("origins");

	private static final Object2ObjectOpenHashMap<Identifier, Origin> ORIGINS_BY_ID = new Object2ObjectOpenHashMap<>();
	private static final ObjectOpenHashSet<Identifier> DISABLED_ORIGINS = new ObjectOpenHashSet<>();

	private static final Object2ObjectOpenHashMap<Identifier, Integer> LOADING_PRIORITIES = new Object2ObjectOpenHashMap<>();
	private static final Gson GSON = new GsonBuilder()
		.disableHtmlEscaping()
		.setPrettyPrinting()
		.create();

	public OriginManager() {
		super(GSON, "origins", ResourceType.SERVER_DATA);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(PowerManager.ID, ID);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(ID, (player, joined) -> send(player));
	}

	@Override
	protected void apply(MultiJsonDataContainer prepared, ResourceManager manager, Profiler profiler) {

		Origins.LOGGER.info("Reading origins from data packs...");

		DynamicRegistryManager dynamicRegistries = CalioServer.getDynamicRegistries().orElse(null);
		startBuilding();

		if (dynamicRegistries == null) {

			Origins.LOGGER.error("Can't read origins from data packs without access to dynamic registries!");
			endBuilding();

			return;

		}

		AtomicBoolean hasConfigChanged = new AtomicBoolean(false);
		prepared.forEach((packName, id, jsonElement) -> {

			try {

				SerializableData.CURRENT_NAMESPACE = id.getNamespace();
				SerializableData.CURRENT_PATH = id.getPath();

				if (!(jsonElement instanceof JsonObject jsonObject)) {
					throw new JsonSyntaxException("Not a JSON object: " + jsonElement);
				}

				jsonObject.addProperty("id", id.toString());
				Origin origin = Origin.DATA_TYPE.read(dynamicRegistries.getOps(JsonOps.INSTANCE), jsonObject).getOrThrow();

				int prevLoadingPriority = LOADING_PRIORITIES.getOrDefault(id, 0);
				int currLoadingPriority = JsonHelper.getInt(jsonObject, "loading_priority", 0);

				if (!contains(id)) {

					origin.validate();

					register(id, origin);
					LOADING_PRIORITIES.put(id, currLoadingPriority);

				}

				else if (prevLoadingPriority < currLoadingPriority) {

					Origins.LOGGER.warn("Overriding origin \"{}\" (with prev. loading priority of {}) with a higher loading priority of {} from data pack [{}]!", id, prevLoadingPriority, currLoadingPriority, packName);
					origin.validate();

					update(id, origin);
					LOADING_PRIORITIES.put(id, currLoadingPriority);

				}

				origin = get(id);
				hasConfigChanged.set(hasConfigChanged.get() | Origins.config.addToConfig(origin));

				if (Origins.config.isOriginDisabled(id)) {
					disable(id);
				}

			}

			catch (Exception e) {
				Origins.LOGGER.error("There was a problem reading origin \"{}\": {}", id, e.getMessage());
			}

		});

		SerializableData.CURRENT_NAMESPACE = null;
		SerializableData.CURRENT_PATH = null;

		Origins.LOGGER.info("Finished reading origins from data packs. Registry contains {} origins.", size());
		endBuilding();

		if (hasConfigChanged.get()) {
			Origins.serializeConfig();
		}

	}

	@Override
	public void onReject(String packName, Identifier resourceId) {

		if (!contains(resourceId)) {
			disable(resourceId);
		}

	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	@Override
	public Collection<Identifier> getFabricDependencies() {
		return DEPENDENCIES;
	}

	public static Set<Map.Entry<Identifier, Origin>> entrySet() {
		return new ObjectOpenHashSet<>(ORIGINS_BY_ID.object2ObjectEntrySet());
	}

	public static Set<Identifier> keySet() {
		return new ObjectOpenHashSet<>(ORIGINS_BY_ID.keySet());
	}

	public static Collection<Origin> values() {
		return new ObjectOpenHashSet<>(ORIGINS_BY_ID.values());
	}

	public static DataResult<Origin> getResult(Identifier id) {
		return contains(id)
			? DataResult.success(ORIGINS_BY_ID.get(id))
			: DataResult.error(() -> "Could not get origin from ID \"" + id + "\", as it was not registered!");
	}

	public static Optional<Origin> getOptional(Identifier id) {
		return getResult(id).result();
	}

	@Nullable
	public static Origin getNullable(Identifier id) {
		return ORIGINS_BY_ID.get(id);
	}

	public static Origin get(Identifier id) {
		return getResult(id).getOrThrow();
	}

	public static boolean contains(Origin origin) {
		return contains(origin.getId());
	}

	public static boolean contains(Identifier id) {
		return ORIGINS_BY_ID.containsKey(id);
	}

	public static int size() {
		return ORIGINS_BY_ID.size();
	}

	private static void startBuilding() {

		LOADING_PRIORITIES.clear();

		ORIGINS_BY_ID.clear();
		DISABLED_ORIGINS.clear();

	}

	private static void endBuilding() {

		LOADING_PRIORITIES.clear();
		ORIGINS_BY_ID.put(Origin.EMPTY.getId(), Origin.EMPTY);

		ORIGINS_BY_ID.trim();
		DISABLED_ORIGINS.trim();

	}

	private static Origin register(Identifier id, Origin origin) {

		if (contains(id)) {
			throw new IllegalArgumentException("Tried to register duplicate origin with ID \"" + id + "\"!");
		}

		else {

			DISABLED_ORIGINS.remove(id);
			ORIGINS_BY_ID.put(id, origin);

			return origin;

		}

	}

	private static Origin remove(Identifier id) {
		return ORIGINS_BY_ID.remove(id);
	}

	private static Origin update(Identifier id, Origin origin) {
		remove(id);
		return register(id, origin);
	}

	public static boolean isDisabled(Identifier id) {
		return DISABLED_ORIGINS.contains(id);
	}

	public static void disable(Identifier id) {
		remove(id);
		DISABLED_ORIGINS.add(id);
	}

	public static void send(ServerPlayerEntity player) {

		if (player.server.isRemote()) {
			ServerPlayNetworking.send(player, new SyncOriginsS2CPacket(ORIGINS_BY_ID));
		}

	}

	@Environment(EnvType.CLIENT)
	public static void receive(SyncOriginsS2CPacket packet, ClientPlayNetworking.Context context) {

		startBuilding();

		packet.originsById().entrySet()
			.stream()
			.peek(e -> e.getValue().validate())
			.forEach(e -> update(e.getKey(), e.getValue()));

		endBuilding();

	}

}
