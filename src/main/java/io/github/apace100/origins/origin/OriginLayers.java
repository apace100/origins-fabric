package io.github.apace100.origins.origin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.integration.OriginDataLoadedCallback;
import io.github.apace100.origins.networking.packet.s2c.OpenChooseOriginScreenS2CPacket;
import io.github.apace100.origins.networking.packet.s2c.SyncOriginLayerRegistryS2CPacket;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.util.*;

public class OriginLayers extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Identifier PHASE = Origins.identifier("phase/origin_layers");

    private static final HashMap<Identifier, OriginLayer> LAYERS = new HashMap<>();
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    private static int minLayerPriority = Integer.MIN_VALUE;

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
        } else {
            ServerPlayNetworking.send(player, new OpenChooseOriginScreenS2CPacket(true));
        }

    }

    @Override
    protected void apply(List<MultiJsonDataContainer> prepared, ResourceManager manager, Profiler profiler) {

        clear();
        Map<Identifier, Map<Integer, List<JsonObject>>> loadedLayers = new HashMap<>();

        Origins.LOGGER.info("Loading origin layer from data files...");
        prepared.forEach(multiJsonDataContainer -> multiJsonDataContainer.forEach((packName, fileId, jsonElement) -> {
            try {

             minLayerPriority = Integer.MIN_VALUE;
             Origins.LOGGER.info("Trying to read origin layer file \"{}\"", fileId);

             JsonObject jsonObject = jsonElement.getAsJsonObject();
             boolean replace = JsonHelper.getBoolean(jsonObject, "replace", false);
             int loadingPriority = JsonHelper.getInt(jsonObject, "loading_priority", 0);


             if (loadingPriority < minLayerPriority) {
                 return;
             }

             Map<Integer, List<JsonObject>> prioritizedUnparsedLayers = loadedLayers.computeIfAbsent(fileId, k -> new HashMap<>());
             List<JsonObject> unparsedLayers = prioritizedUnparsedLayers.computeIfAbsent(loadingPriority, i -> new LinkedList<>());

             if (replace) {
                 unparsedLayers.clear();
                 minLayerPriority = loadingPriority + 1;
             }

             unparsedLayers.add(jsonObject);

            } catch (Exception e) {
                Origins.LOGGER.error("There was a problem reading origin layer file \"{}\" (skipping): {}", fileId, e.getMessage());
            }
        }));

        Origins.LOGGER.info("Loaded {} origin layers. Merging...", loadedLayers.size());
        loadedLayers.forEach((id, prioritizedUnparsedLayers) -> {

            OriginLayer[] layer = {null};
            List<Integer> priorities = prioritizedUnparsedLayers.keySet()
                .stream()
                .sorted()
                .toList();

            for (int priority : priorities) {
                for (JsonObject unparsedLayer : prioritizedUnparsedLayers.get(priority)) {

                    if (layer[0] == null) {
                        layer[0] = OriginLayer.fromJson(id, unparsedLayer);
                    } else {
                        layer[0].merge(unparsedLayer);
                    }

                }
            }

            OriginLayers.register(id, layer[0]);

        });

        Origins.LOGGER.info("Finished loading and merging origin layers from data files. Read {} origin layers.", loadedLayers.size());
        OriginDataLoadedCallback.EVENT.invoker().onDataLoaded(false);

    }

    public static OriginLayer getLayer(Identifier id) {

        if (!LAYERS.containsKey(id)) {
            throw new IllegalArgumentException("Could not get layer from id '" + id.toString() + "', as it doesn't exist!");
        }

        else return LAYERS.get(id);

    }

    public static void register(Identifier id, OriginLayer layer) {

        if (LAYERS.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate origin layer id tried to register: '" + id + "'");
        }

        LAYERS.put(id, layer);

    }

    public static Collection<OriginLayer> getLayers() {
        return LAYERS.values();
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
