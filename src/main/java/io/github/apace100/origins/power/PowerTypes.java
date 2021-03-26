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

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("rawtypes")
public class PowerTypes extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static String CURRENT_NAMESPACE = "";
    public static String CURRENT_PATH = "";

    private static final Identifier MULTIPLE = Origins.identifier("multiple");
    private static final Identifier SIMPLE = Origins.identifier("simple");

    public static final PowerType<Power> WATER_BREATHING;
    public static final PowerType<Power> CONDUIT_POWER_ON_LAND;
    public static final PowerType<Power> AQUA_AFFINITY;
    public static final PowerType<ToggleNightVisionPower> WATER_VISION;
    public static final PowerType<Power> LIKE_WATER;
    public static final PowerType<CooldownPower> WEBBING;
    public static final PowerType<TogglePower> CLIMBING;
    public static final PowerType<Power> NO_COBWEB_SLOWDOWN;
    public static final PowerType<Power> MASTER_OF_WEBS_NO_SLOWDOWN;
    public static final PowerType<Power> SLOW_FALLING;
    public static final PowerType<Power> SCARE_CREEPERS;

    static {
        WATER_BREATHING = new PowerTypeReference<>(new Identifier(Origins.MODID, "water_breathing"));
        CONDUIT_POWER_ON_LAND = new PowerTypeReference<>(new Identifier(Origins.MODID, "conduit_power_on_land"));
        AQUA_AFFINITY = new PowerTypeReference<>(new Identifier(Origins.MODID, "aqua_affinity"));
        WATER_VISION = new PowerTypeReference<>(new Identifier(Origins.MODID, "water_vision"));
        LIKE_WATER = new PowerTypeReference<>(new Identifier(Origins.MODID, "like_water"));
        WEBBING = new PowerTypeReference<>(new Identifier(Origins.MODID, "webbing"));
        CLIMBING = new PowerTypeReference<>(new Identifier(Origins.MODID, "climbing"));
        NO_COBWEB_SLOWDOWN = new PowerTypeReference<>(new Identifier(Origins.MODID, "no_cobweb_slowdown"));
        MASTER_OF_WEBS_NO_SLOWDOWN = new PowerTypeReference<>(new Identifier(Origins.MODID, "master_of_webs_no_slowdown"));
        SLOW_FALLING = new PowerTypeReference<>(new Identifier(Origins.MODID, "slow_falling"));
        SCARE_CREEPERS = new PowerTypeReference<>(new Identifier(Origins.MODID, "scare_creepers"));
    }

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private final HashMap<Identifier, Integer> loadingPriorities = new HashMap<>();

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
                    CURRENT_NAMESPACE = id.getNamespace();
                    CURRENT_PATH = id.getPath();
                    JsonObject jo = je.getAsJsonObject();
                    Identifier factoryId = Identifier.tryParse(JsonHelper.getString(jo, "type"));
                    if(MULTIPLE.equals(factoryId)) {
                        List<Identifier> subPowers = new LinkedList<>();
                        for(Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                            if( entry.getKey().equals("type")
                            ||  entry.getKey().equals("loading_priority")
                            ||  entry.getKey().equals("name")
                            ||  entry.getKey().equals("description")
                            ||  entry.getKey().equals("hidden")
                            ||  entry.getKey().equals("condition")) {
                                continue;
                            }
                            Identifier subId = new Identifier(id.toString() + "_" + entry.getKey());
                            try {
                                readPower(subId, entry.getValue(), true);
                                subPowers.add(subId);
                            } catch(Exception e) {
                                Origins.LOGGER.error("There was a problem reading sub-power \"" +
                                    subId.toString() + "\" in power file \"" + id.toString() + "\": " + e.getMessage());
                            }
                        }
                        MultiplePowerType superPower = (MultiplePowerType)readPower(id, je, false, MultiplePowerType::new);
                        superPower.setSubPowers(subPowers);
                    } else {
                        readPower(id, je, false);
                    }
                } catch(Exception e) {
                    Origins.LOGGER.error("There was a problem reading power file " + id.toString() + " (skipping): " + e.getMessage());
                }
            });
        });
        loadingPriorities.clear();
        CURRENT_NAMESPACE = null;
        CURRENT_PATH = null;
        Origins.LOGGER.info("Finished loading powers from data files. Registry contains " + PowerTypeRegistry.size() + " powers.");
    }

    private void readPower(Identifier id, JsonElement je, boolean isSubPower) {
        readPower(id, je, isSubPower, PowerType::new);
    }

    private PowerType readPower(Identifier id, JsonElement je, boolean isSubPower,
                                BiFunction<Identifier, PowerFactory.Instance, PowerType> powerTypeFactory) {
        JsonObject jo = je.getAsJsonObject();
        Identifier factoryId = Identifier.tryParse(JsonHelper.getString(jo, "type"));
        if(MULTIPLE.equals(factoryId)) {
            factoryId = SIMPLE;
            if(isSubPower) {
                throw new JsonSyntaxException("Power type \"" + MULTIPLE.toString() + "\" may not be used for a sub-power of "
                    + "another \"" + MULTIPLE.toString() + "\" power.");
            }
        }
        Optional<PowerFactory> optionalFactory = ModRegistries.POWER_FACTORY.getOrEmpty(factoryId);
        if(!optionalFactory.isPresent()) {
            throw new JsonSyntaxException("Power type \"" + factoryId.toString() + "\" is not defined.");
        }
        PowerFactory.Instance factoryInstance = optionalFactory.get().read(jo);
        PowerType type = powerTypeFactory.apply(id, factoryInstance);
        int priority = JsonHelper.getInt(jo, "loading_priority", 0);
        String name = JsonHelper.getString(jo, "name", "");
        String description = JsonHelper.getString(jo, "description", "");
        boolean hidden = JsonHelper.getBoolean(jo, "hidden", false);
        if(hidden || isSubPower) {
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
        return type;
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
