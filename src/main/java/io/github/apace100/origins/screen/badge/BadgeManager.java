package io.github.apace100.origins.screen.badge;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.integration.PostPowerLoadCallback;
import io.github.apace100.apoli.integration.PrePowerReloadCallback;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.NamespaceAlias;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.origins.Origins;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class BadgeManager {

    public static final Registry<BadgeFactory<?>> BADGE_REGISTRY = FabricRegistryBuilder.createSimple(ClassUtil.<BadgeFactory<?>>castClass(BadgeFactory.class), Origins.identifier("badge")).buildAndRegister();
    public static final BadgeFactory<Badge> SIMPLE = registerBadgeFactory(Badge.simpleBadgeFactory());
    public static final BadgeFactory<TooltipBadge> TOOLTIP = registerBadgeFactory(TooltipBadge.tooltipBadgeFactory());
    public static final BadgeFactory<KeyBindingBadge> KEY_BINDING = registerBadgeFactory(KeyBindingBadge.keyBindingBadgeFactory());
    public static final BadgeFactory<CraftingRecipeBadge> CRAFTING_RECIPE = registerBadgeFactory(CraftingRecipeBadge.craftingRecipeBadgeFactory());

    private final HashMap<Identifier, LinkedList<BadgeFactory.Instance>> badgeFactories = new HashMap<>();
    @Environment(EnvType.CLIENT)
    private final HashMap<Identifier, LinkedList<Badge>> badges = new HashMap<>();

    public BadgeManager() {
        PrePowerReloadCallback.EVENT.register(this::clearBadgeFactories);
        PowerTypes.registerAdditionalData("badges", (powerId, factoryId, isSubPower, data, powerType) -> {
            if(!powerType.isHidden()) {
                if(data.isJsonArray()) {
                    this.badgeFactories.put(powerId, new LinkedList<>());
                    for(JsonElement badgeJson : data.getAsJsonArray()) {
                        if(badgeJson.isJsonObject()) {
                            addBadge(powerId, read(powerType, badgeJson.getAsJsonObject()));
                        } else {
                            Origins.LOGGER.error("\"badges\" field in power \"" + powerId
                                + "\" contained an entry that was not a JSON object.");
                        }
                    }
                } else {
                    Origins.LOGGER.error("\"badges\" field in power \"" + powerId + "\" should be an array.");
                }
            }
        });
        PostPowerLoadCallback.EVENT.register((powerId, factoryId, isSubPower, data, powerType) -> {
            if(!badgeFactories.containsKey(powerId) || badgeFactories.get(powerId).size() == 0) {
                if(powerType instanceof MultiplePowerType<?> mp) {
                    for(Identifier sp : mp.getSubPowers()) {
                        if(PowerTypeRegistry.contains(sp)) {
                            addAutoBadge(PowerTypeRegistry.get(sp), powerId);
                        }
                    }
                } else {
                    this.addAutoBadge(powerType, powerId);
                }
            }
        });
    }

    public void addAutoBadge(PowerType<?> powerType, Identifier powerId) {
        Power power = powerType.create(null);
        if(power instanceof Active active) {
            addBadge(powerId, autoActiveBadge(powerType, active));
        } else if(power instanceof RecipePower recipePower) {
            addBadge(powerId, autoRecipeBadge(powerType, recipePower));
        }
    }

    public static BadgeFactory.Instance autoActiveBadge(PowerType<?> powerType, Active active) {
        String name = active instanceof TogglePower || active instanceof ToggleNightVisionPower ? "toggle" : "active";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("sprite", Origins.identifier("textures/gui/badge/" + name + ".png").toString());
        jsonObject.addProperty("text", "origins.gui.badge." + name);
        return KEY_BINDING.read(powerType, jsonObject);
    }

    public static BadgeFactory.Instance autoRecipeBadge(PowerType<?> powerType, RecipePower recipePower) {
        String name = recipePower.getRecipe() instanceof ShapedRecipe ? "shaped" : "shapeless";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("sprite", Origins.identifier("textures/gui/badge/recipe.png").toString());
        jsonObject.addProperty("text", "origins.gui.badge.recipe.crafting." + name);
        return CRAFTING_RECIPE.read(powerType, jsonObject);
    }

    public static <B extends Badge> BadgeFactory<B> registerBadgeFactory(BadgeFactory<B> factory) {
        return Registry.register(BADGE_REGISTRY, factory.getSerializerId(), factory);
    }

    public static BadgeFactory<?> readFactory(JsonObject jsonObject) {
        String factoryName = JsonHelper.getString(jsonObject, "type", Origins.identifier("tooltip").toString());
        Identifier factoryId = new Identifier(factoryName);
        Optional<BadgeFactory<?>> optionalFactory = BADGE_REGISTRY.getOrEmpty(factoryId);
        if(optionalFactory.isEmpty()) {
            if(NamespaceAlias.hasAlias(factoryId)) {
                optionalFactory = BADGE_REGISTRY.getOrEmpty(NamespaceAlias.resolveAlias(factoryId));
            }
            if(optionalFactory.isEmpty()) {
                throw new JsonSyntaxException("Badge type \"" + factoryId + "\" is not defined.");
            }
        }
        return optionalFactory.get();
    }

    public static BadgeFactory.Instance read(PowerType<?> powerType, JsonObject jsonObject) {
        return readFactory(jsonObject).read(powerType, jsonObject);
    }

    public static BadgeFactory.Instance read(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return readFactory(jsonObject).read(jsonObject);
        }
        throw new JsonSyntaxException("Badge has to be a JsonObject!");
    }

    public static BadgeFactory.Instance read(PacketByteBuf buf) {
        Identifier factoryId = Identifier.tryParse(buf.readString());
        Optional<BadgeFactory<?>> factory = BADGE_REGISTRY.getOrEmpty(factoryId);
        return factory.orElseThrow(() -> new RuntimeException("Recieved unknown badge type " + factoryId + " from server!")).read(buf);
    }

    public void writeSyncData(PacketByteBuf buf) {
        buf.writeInt(badgeFactories.size());
        badgeFactories.forEach((id, list) -> {
            buf.writeIdentifier(id);
            buf.writeInt(list.size());
            list.forEach(badge -> badge.write(buf));
        });
    }

    public void addBadge(Identifier powerId, BadgeFactory.Instance badge) {
        List<BadgeFactory.Instance> badgeList = badgeFactories.computeIfAbsent(powerId, id -> new LinkedList<>());
        badgeList.add(badge);
    }

    public void clearBadgeFactories() {
        badgeFactories.clear();
    }

    @Environment(EnvType.CLIENT)
    public void clearBadges() {
        this.clearBadgeFactories();
        badges.clear();
    }

    @Environment(EnvType.CLIENT)
    public List<Badge> getBadges(Identifier powerId) {
        if(badges.containsKey(powerId)) {
            return badges.get(powerId);
        } else if(badgeFactories.containsKey(powerId)) {
            LinkedList<Badge> badges = new LinkedList<>();
            this.badgeFactories.get(powerId).forEach(badge -> badges.add(badge.get()));
            this.badges.put(powerId, badges);
            return badges;
        } else  {
            return Lists.newArrayList();
        }
    }

}
