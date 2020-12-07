package io.github.apace100.origins.origin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.integration.OriginDataLoadedCallback;
import io.github.apace100.origins.util.MultiJsonDataLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OriginLayers extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

    private static HashMap<Identifier, OriginLayer> layers = new HashMap<>();

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public OriginLayers() {
        super(GSON, "origin_layers");
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> loader, ResourceManager manager, Profiler profiler) {
        clear();
        loader.forEach((id, jel) -> {
            jel.forEach(je -> {
                try {
                    Origins.LOGGER.info("Trying to read layer file: " + id);
                    JsonObject jo = je.getAsJsonObject();
                    boolean replace = JsonHelper.getBoolean(jo, "replace", false);
                    if (layers.containsKey(id)) {
                        if (replace) {
                            OriginLayer layer = OriginLayer.fromJson(id, jo);
                            layers.put(id, layer);
                        } else {
                            Origins.LOGGER.info("Merging origin layer " + id.toString());
                            layers.get(id).merge(jo);
                        }
                    } else {
                        OriginLayer layer = OriginLayer.fromJson(id, jo);
                        layers.put(id, layer);
                    }
                } catch (Exception e) {
                    Origins.LOGGER.error("There was a problem reading Origin layer file " + id.toString() + " (skipping): " + e.getMessage());
                }
            });
        });
        Origins.LOGGER.info("Finished loading origin layers from data files. Read " + layers.size() + " layers.");
        OriginDataLoadedCallback.EVENT.invoker().onDataLoaded(false);
    }

    public static OriginLayer getLayer(Identifier id) {
        return layers.get(id);
    }

    public static Collection<OriginLayer> getLayers() {
        return layers.values();
    }

    public static int size() {
        return layers.size();
    }

    public static void clear() {
        layers.clear();
    }

    public static void add(OriginLayer layer) {
        layers.put(layer.getIdentifier(), layer);
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Origins.MODID, "origin_layers");
    }
}
