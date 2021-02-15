package io.github.apace100.origins.power;

import com.google.gson.*;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.MultiJsonDataLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PowerTypes extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final PowerType<Power> WATER_BREATHING;
    public static final PowerType<Power> CONDUIT_POWER_ON_LAND;
    public static final PowerType<Power> AQUA_AFFINITY;
    public static final PowerType<ToggleNightVisionPower> WATER_VISION;
    public static final PowerType<Power> LIKE_WATER;
    public static final PowerType<CooldownPower> WEBBING;
    public static final PowerType<TogglePower> CLIMBING;
    public static final PowerType<Power> NO_COBWEB_SLOWDOWN;
    public static final PowerType<Power> SLOW_FALLING;
    public static final PowerType<Power> SCARE_CREEPERS;
    public static final PowerType<PreventItemUsePower> PUMPKIN_HATE;

    static {
        WATER_BREATHING = new PowerTypeReference<>(new Identifier(Origins.MODID, "water_breathing"));
        CONDUIT_POWER_ON_LAND = new PowerTypeReference<>(new Identifier(Origins.MODID, "conduit_power_on_land"));
        AQUA_AFFINITY = new PowerTypeReference<>(new Identifier(Origins.MODID, "aqua_affinity"));
        WATER_VISION = new PowerTypeReference<>(new Identifier(Origins.MODID, "water_vision"));
        LIKE_WATER = new PowerTypeReference<>(new Identifier(Origins.MODID, "like_water"));
        WEBBING = new PowerTypeReference<>(new Identifier(Origins.MODID, "webbing"));
        CLIMBING = new PowerTypeReference<>(new Identifier(Origins.MODID, "climbing"));
        NO_COBWEB_SLOWDOWN = new PowerTypeReference<>(new Identifier(Origins.MODID, "no_cobweb_slowdown"));
        SLOW_FALLING = new PowerTypeReference<>(new Identifier(Origins.MODID, "slow_falling"));
        SCARE_CREEPERS = new PowerTypeReference<>(new Identifier(Origins.MODID, "scare_creepers"));
        PUMPKIN_HATE = new PowerTypeReference<>(new Identifier(Origins.MODID, "pumpkin_hate"));
    }

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private HashMap<Identifier, Integer> loadingPriorities = new HashMap<>();

    public PowerTypes() {
        super(GSON, "powers");
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> loader, ResourceManager manager, Profiler profiler) {
        PowerTypeRegistry.reset();
        loadingPriorities.clear();
        loader.forEach((id, jel) -> {
            jel.forEach(je -> {
                try {
                    JsonObject jo = je.getAsJsonObject();
                    Identifier factoryId = Identifier.tryParse(JsonHelper.getString(jo, "type"));
                    Optional<PowerFactory> optionalFactory = ModRegistries.POWER_FACTORY.getOrEmpty(factoryId);
                    if(!optionalFactory.isPresent()) {
                        throw new JsonSyntaxException("Power type \"" + factoryId.toString() + "\" is not defined.");
                    }
                    PowerFactory.Instance factoryInstance = optionalFactory.get().read(jo);
                    PowerType type = new PowerType(id, factoryInstance);
                    int priority = JsonHelper.getInt(jo, "loading_priority", 0);
                    String name = JsonHelper.getString(jo, "name", "");
                    String description = JsonHelper.getString(jo, "description", "");
                    boolean hidden = JsonHelper.getBoolean(jo, "hidden", false);
                    if(hidden) {
                        type.setHidden();
                    }
                    type.setTranslationKeys(name, description);
                    if(!PowerTypeRegistry.contains(id)) {
                        PowerTypeRegistry.register(id, type);
                        loadingPriorities.put(id, priority);
                    } else {
                        if(loadingPriorities.get(id) < priority) {
                            PowerTypeRegistry.update(id, type);
                            loadingPriorities.put(id, priority);
                        }
                    }
                } catch(Exception e) {
                    Origins.LOGGER.error("There was a problem reading power file " + id.toString() + " (skipping): " + e.getMessage());
                }
            });
        });
        loadingPriorities.clear();
        Origins.LOGGER.info("Finished loading powers from data files. Registry contains " + PowerTypeRegistry.size() + " powers.");
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Origins.MODID, "powers");
    }

    private static <T extends Power> PowerType<T> register(String path, PowerType<T> type) {
        return new PowerTypeReference<>(new Identifier(Origins.MODID, path));
        //return PowerTypeRegistry.register(new Identifier(Origins.MODID, path), type);
    }
}
