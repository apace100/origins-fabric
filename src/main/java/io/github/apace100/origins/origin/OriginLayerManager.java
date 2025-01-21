package io.github.apace100.origins.origin;

import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.util.PrioritizedEntry;
import io.github.apace100.calio.CalioServer;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.integration.CarpetIntegration;
import io.github.apace100.origins.integration.OriginDataLoadedCallback;
import io.github.apace100.origins.networking.packet.s2c.OpenChooseOriginScreenS2CPacket;
import io.github.apace100.origins.networking.packet.s2c.SyncOriginLayersS2CPacket;
import io.github.apace100.origins.registry.ModComponents;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class OriginLayerManager extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Set<Identifier> DEPENDENCIES = Util.make(new ObjectOpenHashSet<>(), set -> set.add(OriginManager.ID));
    public static final Identifier ID = Origins.identifier("origin_layers");

    private static final Object2ObjectOpenHashMap<Identifier, OriginLayer> LAYERS_BY_ID = new Object2ObjectOpenHashMap<>();

    private static final Map<Identifier, Integer> LOADING_PRIORITIES = new HashMap<>();
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    public OriginLayerManager() {
        super(GSON, "origin_layers", ResourceType.SERVER_DATA);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(OriginManager.ID, ID);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(ID, (player, joined) -> {

            OriginComponent component = ModComponents.ORIGIN.get(player);
            values().stream()
                .filter(OriginLayer::isEnabled)
                .filter(Predicate.not(component::hasOrigin))
                .forEach(layer -> component.setOrigin(layer, Origin.EMPTY));

            send(player);
            updateData(player, joined);

        });
    }

    private void updateData(ServerPlayerEntity player, boolean init) {

        RegistryOps<JsonElement> jsonOps = player.getRegistryManager().getOps(JsonOps.INSTANCE);
        OriginComponent component = ModComponents.ORIGIN.get(player);

        int mismatches = 0;

        for (Map.Entry<OriginLayer, Origin> entry : component.getOrigins().entrySet()) {

            OriginLayer oldLayer = entry.getKey();
            OriginLayer newLayer = OriginLayerManager.getNullable(oldLayer.getId());

            Origin oldOrigin = entry.getValue();
            Origin newOrigin = OriginManager.getNullable(oldOrigin.getId());

            if (oldOrigin != Origin.EMPTY) {

                if (newLayer == null) {
                    Origins.LOGGER.error("Removed unregistered origin layer \"{}\" from player {}!", oldLayer.getId(), player.getName().getString());
                    component.removeLayer(oldLayer);
                }

                else if (!newLayer.contains(oldOrigin) || newOrigin == null) {
                    Origins.LOGGER.error("Removed unregistered origin \"{}\" from origin layer \"{}\" from player {}!", oldOrigin.getId(), oldLayer.getId(), player.getName().getString());
                    component.setOrigin(newLayer, Origin.EMPTY);
                }

                else {

                    JsonElement oldOriginJson = Origin.DATA_TYPE.write(jsonOps, oldOrigin).getOrThrow(JsonParseException::new);
                    JsonElement newOriginJson = Origin.DATA_TYPE.write(jsonOps, newOrigin).getOrThrow(JsonParseException::new);

                    if (oldOriginJson.equals(newOriginJson)) {
                        continue;
                    }

                    Origins.LOGGER.warn("Origin \"{}\" from player {} has mismatched data fields! Updating...", oldOrigin.getId(), player.getName().getString());
                    mismatches++;

                    component.setOrigin(newLayer, newOrigin);

                }

            }

        }

        if (mismatches > 0) {
            Origins.LOGGER.info("Finished updating {} origins with mismatched data fields from player {}!", mismatches, player.getName().getString());
        }

        if (component.hasAllOrigins()) {
            component.sync();
        }

        else {

            component.checkAutoChoosingLayers(player, true);

            if (init) {

                if (component.hasAllOrigins()) {
                    OriginComponent.onChosen(player, false);
                }

                else if (!CarpetIntegration.isPlayerFake(player)) {
                    component.selectingOrigin(true);
                    ServerPlayNetworking.send(player, new OpenChooseOriginScreenS2CPacket(true));
                }

            }

            component.sync();

        }

    }

    @Override
    protected void apply(MultiJsonDataContainer prepared, ResourceManager manager, Profiler profiler) {

        Origins.LOGGER.info("Reading origin layers from data packs...");

        DynamicRegistryManager dynamicRegistries = CalioServer.getDynamicRegistries().orElse(null);
        startBuilding();

        if (dynamicRegistries == null) {

            Origins.LOGGER.error("Can't read origin layers from data packs without access to dynamic registries!");
            endBuilding();

            return;

        }

        Map<Identifier, List<PrioritizedEntry<OriginLayer>>> loadedLayers = new HashMap<>();
        Origins.LOGGER.info("Reading origin layers from data packs...");

        prepared.forEach((packName, id, jsonElement) -> {

            try {

                SerializableData.CURRENT_NAMESPACE = id.getNamespace();
                SerializableData.CURRENT_PATH = id.getPath();

                if (!(jsonElement instanceof JsonObject jsonObject)) {
                    throw new JsonSyntaxException("Not a JSON object: " + jsonElement);
                }

                jsonObject.addProperty("id", id.toString());

                OriginLayer layer = OriginLayer.DATA_TYPE.read(dynamicRegistries.getOps(JsonOps.INSTANCE), jsonObject).getOrThrow();
                int currLoadingPriority = JsonHelper.getInt(jsonObject, "loading_priority", 0);

                PrioritizedEntry<OriginLayer> entry = new PrioritizedEntry<>(layer, currLoadingPriority);
                int prevLoadingPriority = LOADING_PRIORITIES.getOrDefault(id, Integer.MIN_VALUE);

                if (layer.shouldReplace() && currLoadingPriority <= prevLoadingPriority) {
                    Origins.LOGGER.warn("Ignoring origin layer \"{}\" with 'replace' set to true from data pack [{}]. Its loading priority ({}) must be higher than {} to replace the origin layer!", id, packName, currLoadingPriority, prevLoadingPriority);
                }

                else {

                    if (layer.shouldReplace()) {
                        Origins.LOGGER.info("Origin layer \"{}\" has been replaced by data pack [{}]!", id, packName);
                    }

                    List<String> invalidOrigins = layer.getConditionedOrigins()
                        .stream()
                        .map(OriginLayer.ConditionedOrigin::origins)
                        .flatMap(Collection::stream)
                        .filter(Predicate.not(OriginManager::contains))
                        .map(Identifier::toString)
                        .toList();

                    if (!invalidOrigins.isEmpty()) {
                        Origins.LOGGER.error("Origin layer \"{}\" contained {} invalid origin(s): {}", id, invalidOrigins.size(), String.join(", ", invalidOrigins));
                    }

                    loadedLayers.computeIfAbsent(id, k -> new LinkedList<>()).add(entry);
                    LOADING_PRIORITIES.put(id, currLoadingPriority);

                }

            }

            catch (Exception e) {
                Origins.LOGGER.error("There was a problem reading origin layer \"{}\": {}", id, e.getMessage());
            }

        });

        SerializableData.CURRENT_NAMESPACE = null;
        SerializableData.CURRENT_PATH = null;

        Origins.LOGGER.info("Finished reading {} origin layers. Merging similar origin layers...", loadedLayers.size());
        loadedLayers.forEach((id, entries) -> {

            AtomicReference<OriginLayer> currentLayer = new AtomicReference<>();
            entries.sort(Comparator.comparing(PrioritizedEntry::priority));

            for (PrioritizedEntry<OriginLayer> entry : entries) {

                if (currentLayer.get() == null) {
                    currentLayer.set(entry.value());
                }

                else {
                    currentLayer.accumulateAndGet(entry.value(), OriginLayerManager::merge);
                }

            }

            LAYERS_BY_ID.put(id, currentLayer.get());

        });

        endBuilding();
        Origins.LOGGER.info("Finished merging similar origin layers. Registry contains {} origin layers.", size());

        OriginDataLoadedCallback.EVENT.invoker().onDataLoaded(false);

    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return DEPENDENCIES;
    }

    private static OriginLayer merge(OriginLayer oldLayer, OriginLayer newLayer) {

        if (newLayer.shouldReplace()) {
            return newLayer;
        }

        else {

            Set<OriginLayer.ConditionedOrigin> origins = new ObjectLinkedOpenHashSet<>(oldLayer.getConditionedOrigins());
            Set<Identifier> originsExcludedFromRandom = new ObjectLinkedOpenHashSet<>(oldLayer.getOriginsExcludedFromRandom());

            if (newLayer.shouldReplaceOrigins()) {
                origins.clear();
            }

            if (newLayer.shouldReplaceExcludedOriginsFromRandom()) {
                originsExcludedFromRandom.clear();
            }

            origins.addAll(newLayer.getConditionedOrigins());
            originsExcludedFromRandom.addAll(newLayer.getOriginsExcludedFromRandom());

            return new OriginLayer(
                oldLayer.getId(),
                oldLayer.getOrder(),
                origins,
                newLayer.shouldReplaceOrigins(),
                newLayer.shouldReplace(),
                oldLayer.isEnabled(),
                oldLayer.getName(),
                oldLayer.getGuiTitle(),
                oldLayer.getMissingName(),
                oldLayer.getMissingDescription(),
                oldLayer.isRandomAllowed(),
                oldLayer.isUnchoosableRandomAllowed(),
                originsExcludedFromRandom,
                newLayer.shouldReplaceExcludedOriginsFromRandom(),
                oldLayer.getDefaultOrigin(),
                oldLayer.shouldAutoChoose(),
                oldLayer.isHidden()
            );

        }

    }

    public static DataResult<OriginLayer> getResult(Identifier id) {
        return LAYERS_BY_ID.containsKey(id)
            ? DataResult.success(LAYERS_BY_ID.get(id))
            : DataResult.error(() -> "Could not get layer from id '" + id.toString() + "', as it doesn't exist!");
    }

    public static Optional<OriginLayer> getOptional(Identifier id) {
        return getResult(id).result();
    }

    @Nullable
    public static OriginLayer getNullable(Identifier id) {
        return LAYERS_BY_ID.get(id);
    }

    public static OriginLayer get(Identifier id) {
        return getResult(id).getOrThrow();
    }

    public static Set<Map.Entry<Identifier, OriginLayer>> entrySet() {
        return new ObjectOpenHashSet<>(LAYERS_BY_ID.entrySet());
    }

    public static Set<Identifier> keySet() {
        return new ObjectOpenHashSet<>(LAYERS_BY_ID.keySet());
    }

    public static Collection<OriginLayer> values() {
        return new ObjectOpenHashSet<>(LAYERS_BY_ID.values());
    }

    public static boolean contains(OriginLayer layer) {
        return contains(layer.getId());
    }

    public static boolean contains(Identifier id) {
        return LAYERS_BY_ID.containsKey(id);
    }

    public static int getOriginOptionCount(PlayerEntity playerEntity) {
        return getOriginOptionCount(playerEntity, (layer, component) -> !component.hasOrigin(layer));
    }

    public static int getOriginOptionCount(PlayerEntity playerEntity, BiPredicate<OriginLayer, OriginComponent> condition) {
        return values()
            .stream()
            .filter(ol -> ol.isEnabled() && ModComponents.ORIGIN.maybeGet(playerEntity).map(oc -> condition.test(ol, oc)).orElse(false))
            .flatMapToInt(ol -> IntStream.of(ol.getOriginOptionCount(playerEntity)))
            .sum();
    }

    public static int size() {
        return LAYERS_BY_ID.size();
    }

    private static void startBuilding() {
        LOADING_PRIORITIES.clear();
        LAYERS_BY_ID.clear();
    }

    private static void endBuilding() {
        LOADING_PRIORITIES.clear();
        LAYERS_BY_ID.trim();
    }

    public static void send(ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, new SyncOriginLayersS2CPacket(LAYERS_BY_ID));
    }

    @Environment(EnvType.CLIENT)
    public static void receive(SyncOriginLayersS2CPacket packet, ClientPlayNetworking.Context context) {

        startBuilding();
        LAYERS_BY_ID.putAll(packet.layersById());

        endBuilding();
        OriginDataLoadedCallback.EVENT.invoker().onDataLoaded(true);

    }

}
