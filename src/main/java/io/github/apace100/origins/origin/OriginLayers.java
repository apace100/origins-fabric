package io.github.apace100.origins.origin;

import carpet.patches.EntityPlayerMPFake;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.integration.OriginDataLoadedCallback;
import io.github.apace100.origins.networking.packet.s2c.OpenChooseOriginScreenS2CPacket;
import io.github.apace100.origins.networking.packet.s2c.SyncOriginLayerRegistryS2CPacket;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class OriginLayers extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Identifier PHASE = Origins.identifier("phase/origin_layers");

    private static final HashMap<Identifier, OriginLayer> LAYERS = new HashMap<>();
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    private static Identifier prevId = null;
    private static int prevPriority = Integer.MIN_VALUE;

    public OriginLayers() {
        super(GSON, "origin_layers", ResourceType.SERVER_DATA);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(OriginManager.PHASE, PHASE);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(PHASE, (player, joined) -> {

            OriginComponent component = ModComponents.ORIGIN.get(player);
            Map<Identifier, OriginLayer> layers = new HashMap<>();

            for (OriginLayer layer : OriginLayers.getLayers()) {

                layers.put(layer.getIdentifier(), layer);

                if (layer.isEnabled() && !component.hasOrigin(layer)) {
                    component.setOrigin(layer, Origin.EMPTY);
                }

            }

            ServerPlayNetworking.send(player, new SyncOriginLayerRegistryS2CPacket(layers));

            if (joined) {

                List<ServerPlayerEntity> otherPlayers = player.getServerWorld().getServer().getPlayerManager().getPlayerList();
                otherPlayers.remove(player);

                otherPlayers.forEach(otherPlayer -> ModComponents.ORIGIN.syncWith(otherPlayer, (ComponentProvider) player));

            }

            postLoading(player, joined);

        });
    }

    private void postLoading(ServerPlayerEntity player, boolean init) {

        OriginComponent component = ModComponents.ORIGIN.get(player);
        boolean mismatch = false;

        for (Map.Entry<OriginLayer, Origin> entry : component.getOrigins().entrySet()) {

            OriginLayer oldLayer = entry.getKey();
            Origin oldOrigin = entry.getValue();

            boolean originOrLayerNotAvailable = !OriginLayers.contains(oldLayer)
                                             || !OriginRegistry.contains(oldOrigin);
            boolean originUnregistered = OriginLayers.contains(oldLayer)
                                      && !OriginLayers.getLayer(oldLayer.getIdentifier()).contains(oldOrigin);

            if (originOrLayerNotAvailable || originUnregistered) {

                if (oldOrigin == Origin.EMPTY) {
                    continue;
                }

                if (originUnregistered) {
                    Origins.LOGGER.error("Removed unregistered origin \"{}\" from origin layer \"{}\" from player {}!", oldOrigin.getIdentifier(), oldLayer.getIdentifier(), player.getName().getString());
                    component.setOrigin(oldLayer, Origin.EMPTY);
                } else {
                    Origins.LOGGER.error("Removed unregistered origin layer \"{}\" from player {}!", oldLayer.getIdentifier(), player.getName().getString());
                    component.removeLayer(oldLayer);
                }

                continue;

            }

            Origin newOrigin = OriginRegistry.get(oldOrigin.getIdentifier());
            if (oldOrigin.toJson().equals(newOrigin.toJson())) {
                continue;
            }

            Origins.LOGGER.warn("Mismatched data fields of origin \"{}\" from player {}! Updating...", oldOrigin.getIdentifier(), player.getName().getString());
            mismatch = true;

            component.setOrigin(oldLayer, newOrigin);

        }

        if (mismatch) {
            Origins.LOGGER.info("Finished updating origin data of player {}!", player.getName().getString());
        }

        OriginComponent.sync(player);
        if (component.hasAllOrigins()) {
            return;
        }

        if (component.checkAutoChoosingLayers(player, true)) {
            component.sync();
        }

        if (!init) {
            return;
        }

        if (component.hasAllOrigins()) {
            OriginComponent.onChosen(player, false);
        } else if (!isFakePlayer(player)) {

            component.selectingOrigin(true);
            component.sync();

            ServerPlayNetworking.send(player, new OpenChooseOriginScreenS2CPacket(true));

        } else {
            component.sync();
        }

    }

    private static boolean isFakePlayer(ServerPlayerEntity player) {
        return FabricLoader.getInstance().isModLoaded("carpet") && player instanceof EntityPlayerMPFake;
    }

    @Override
    protected void apply(MultiJsonDataContainer prepared, ResourceManager manager, Profiler profiler) {

        clear();
        prevId = null;

        Map<Identifier, List<OriginLayer>> loadedLayers = new HashMap<>();

        Origins.LOGGER.info("Loading origin layer from data files...");
        prepared.forEach((packName, id, jsonElement) -> {
            try {

                SerializableData.CURRENT_NAMESPACE = id.getNamespace();
                SerializableData.CURRENT_PATH = id.getPath();

                if (prevId == null || !prevId.equals(id)) {

                    prevPriority = Integer.MIN_VALUE;
                    prevId = id;

                }

                Origins.LOGGER.info("Trying to read origin layer file \"{}\" from data pack [{}]", id, packName);

                OriginLayer layer = OriginLayer.fromJson(id, jsonElement.getAsJsonObject());
                int loadingPriority = layer.getLoadingPriority();

                if (loadingPriority < prevPriority) {
                    Origins.LOGGER.warn("Ignoring replaced duplicate origin layer \"{}\" with a lower loading priority.", id);
                    return;
                }

                List<String> invalidOrigins = layer.getConditionedOrigins()
                    .stream()
                    .flatMap(co -> co.origins().stream())
                    .filter(Predicate.not(OriginRegistry::contains))
                    .map(Identifier::toString)
                    .toList();

                if (!invalidOrigins.isEmpty()) {
                    Origins.LOGGER.error("Origin layer \"{}\" (from data pack [{}]) contained {} invalid origin(s): {}", layer.id, packName, invalidOrigins.size(), String.join(", ", invalidOrigins));
                }

                List<OriginLayer> layers = loadedLayers.computeIfAbsent(id, k -> new LinkedList<>());

                if (layer.shouldReplaceConditionedOrigins()) {
                    layers.clear();
                    prevPriority = loadingPriority + 1;
                }

                layers.add(layer);

            } catch (Exception e) {
                Origins.LOGGER.error("There was a problem reading origin layer file \"{}\" (skipping): {}", id, e.getMessage());
            }
        });

        Origins.LOGGER.info("Finished loading origin layers. Merging similar origin layers...");
        loadedLayers.forEach((id, layers) -> {

            OriginLayer[] currentLayer = {null};
            List<OriginLayer> sortedLayers = layers
                .stream()
                .sorted(Comparator.comparing(OriginLayer::getLoadingPriority))
                .toList();

            for (OriginLayer layer : sortedLayers) {

                if (currentLayer[0] == null) {
                    currentLayer[0] = layer;
                } else {
                    currentLayer[0].merge(layer);
                }

            }

            OriginLayers.register(id, currentLayer[0]);

        });

        Origins.LOGGER.info("Finished merging similar origin layers from data files. Read {} origin layers.", loadedLayers.size());
        OriginDataLoadedCallback.EVENT.invoker().onDataLoaded(false);

        SerializableData.CURRENT_NAMESPACE = null;
        SerializableData.CURRENT_PATH = null;

    }

    public static OriginLayer getLayer(Identifier id) {

        if (!LAYERS.containsKey(id)) {
            throw new IllegalArgumentException("Could not get layer from id '" + id.toString() + "', as it doesn't exist!");
        }

        else return LAYERS.get(id);

    }

    @Nullable
    public static OriginLayer getNullableLayer(Identifier id) {
        return LAYERS.get(id);
    }

    public static void register(Identifier id, OriginLayer layer) {

        if (LAYERS.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate origin layer id tried to register: '" + id + "'");
        }

        layer.id = id;
        LAYERS.put(id, layer);

    }

    public static Collection<OriginLayer> getLayers() {
        return LAYERS.values();
    }

    public static int getOriginOptionCount(PlayerEntity playerEntity) {
        return getOriginOptionCount(playerEntity, (layer, component) -> !component.hasOrigin(layer));
    }

    public static int getOriginOptionCount(PlayerEntity playerEntity, BiPredicate<OriginLayer, OriginComponent> condition) {
        return LAYERS.values()
            .stream()
            .filter(ol -> ol.isEnabled() && ModComponents.ORIGIN.maybeGet(playerEntity).map(oc -> condition.test(ol, oc)).orElse(false))
            .flatMapToInt(ol -> IntStream.of(ol.getOriginOptionCount(playerEntity)))
            .sum();
    }

    public static boolean contains(OriginLayer layer) {
        return contains(layer.getIdentifier());
    }

    public static boolean contains(Identifier id) {
        return LAYERS.containsKey(id);
    }

    public static int size() {
        return LAYERS.size();
    }

    public static void clear() {
        LAYERS.clear();
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Origins.MODID, "origin_layers");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Set.of(Origins.identifier("origins"));
    }

}
