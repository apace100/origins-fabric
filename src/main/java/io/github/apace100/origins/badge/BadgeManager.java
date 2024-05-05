package io.github.apace100.origins.badge;

import com.google.gson.*;
import io.github.apace100.apoli.integration.PostPowerLoadCallback;
import io.github.apace100.apoli.integration.PrePowerReloadCallback;
import io.github.apace100.apoli.power.*;
import io.github.apace100.calio.registry.DataObjectRegistry;
import io.github.apace100.calio.util.DynamicIdentifier;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.integration.AutoBadgeCallback;
import io.github.apace100.origins.networking.ModPackets;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public final class BadgeManager {

    public static final DataObjectRegistry<Badge> REGISTRY = new DataObjectRegistry.Builder<>(Origins.identifier("badge"), Badge.class)
        .readFromData("badges", true)
        .dataErrorHandler((id, exception) -> Origins.LOGGER.error("Failed to read badge " + id + ", caused by", exception))
        .defaultFactory(BadgeFactories.KEYBIND)
        .buildAndRegister();
    public static final Identifier PHASE = Origins.identifier("phase/badge_manager");

    private static final Map<Identifier, List<Badge>> BADGES = new HashMap<>();

    private static final Identifier TOGGLE_BADGE_SPRITE = Origins.identifier("textures/gui/badge/toggle.png");
    private static final Identifier ACTIVE_BADGE_SPRITE = Origins.identifier("textures/gui/badge/active.png");
    private static final Identifier RECIPE_BADGE_SPRITE = Origins.identifier("textures/gui/badge/recipe.png");

    private static final Identifier TOGGLE_BADGE_ID = Origins.identifier("toggle");
    private static final Identifier ACTIVE_BADGE_ID = Origins.identifier("active");

    public static void init() {
        //register builtin badge types
        register(BadgeFactories.SPRITE);
        register(BadgeFactories.TOOLTIP);
        register(BadgeFactories.CRAFTING_RECIPE);
        register(BadgeFactories.KEYBIND);
        //register callbacks
        PrePowerReloadCallback.EVENT.register(BadgeManager::clear);
        PowerTypes.registerAdditionalData("badges", BadgeManager::readCustomBadges);
        PostPowerLoadCallback.EVENT.register(BadgeManager::readAutoBadges);
        AutoBadgeCallback.EVENT.register(BadgeManager::createAutoBadges);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(PowerTypes.PHASE, PHASE);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(PHASE, (player, joined) -> sync(player));
    }

    public static void sync(ServerPlayerEntity player) {

        PacketByteBuf badgesBuf = PacketByteBufs.create();
        REGISTRY.sync(player);

        badgesBuf.writeMap(BADGES,
            PacketByteBuf::writeIdentifier,
            (valueBuf, badges) -> valueBuf.writeCollection(badges, REGISTRY::writeDataObject));

        ServerPlayNetworking.send(player, ModPackets.BADGE_LIST, badgesBuf);

    }

    public static void register(BadgeFactory factory) {
        REGISTRY.registerFactory(factory.id(), factory);
    }

    public static void putPowerBadge(Identifier powerId, Badge badge) {
        BADGES
            .computeIfAbsent(powerId, id -> new LinkedList<>())
            .add(badge);
    }

    public static void putPowerBadges(Identifier powerId, Collection<Badge> badges) {
        BADGES
            .computeIfAbsent(powerId, id -> new LinkedList<>())
            .addAll(badges);
    }

    public static List<Badge> getPowerBadges(Identifier powerId) {
        return BADGES.getOrDefault(powerId, List.of());
    }

    public static boolean hasPowerBadges(Identifier powerId) {
        return BADGES.containsKey(powerId);
    }

    public static boolean hasPowerBadges(PowerType<?> powerType) {
        return hasPowerBadges(powerType.getIdentifier());
    }

    public static void clear() {
        BADGES.clear();
    }

    public static void readCustomBadges(Identifier powerId, Identifier factoryId, boolean isSubPower, JsonElement data, PowerType<?> powerType) {

        if (powerType.isHidden() || isSubPower) {
            return;
        }

        try {

            if (!(data instanceof JsonArray dataArray)) {
                throw new JsonSyntaxException("\"badges\" should be a JSON array!");
            }

            List<Badge> badges = BADGES.computeIfAbsent(powerId, id -> new LinkedList<>());
            for (JsonElement badgeJson : dataArray) {

                Badge badge;
                if (badgeJson instanceof JsonObject badgeObject) {
                    badge = REGISTRY.readDataObject(badgeObject);
                }

                else if (badgeJson instanceof JsonPrimitive badgePrimitive) {
                    Identifier badgeId = DynamicIdentifier.of(badgePrimitive);
                    badge = REGISTRY.get(badgeId);
                }

                else {
                    throw new JsonSyntaxException("Nested JSON arrays are not allowed!");
                }

                badges.add(badge);

            }

        } catch (Exception e) {
            Origins.LOGGER.error("There was a problem parsing badges of power \"{}\": {}", powerId, e.getMessage());
        }

    }

    /**
     *  <p>Attempts to generate badges automatically for each power post-registration. Badges will only be generated if the power fulfills
     *  certain conditions:</p>
     *
     *  <ol>
     *      <li>The power doesn't have any badges defined in the {@code badges} field of its JSON.</li>
     *      <li>The power doesn't use the {@code multiple} power type.
     *      <li>The power is not manually hidden.</li>
     *  </ol>
     */
    public static void readAutoBadges(Identifier powerId, Identifier factoryId, boolean isSubPower, JsonObject json, PowerType<?> powerType) {

        if (!hasPowerBadges(powerId) && !(powerType instanceof MultiplePowerType<?>) && (isSubPower || !powerType.isHidden())) {
            AutoBadgeCallback.EVENT
                .invoker()
                .createAutoBadge(powerId, powerType, BADGES.computeIfAbsent(powerId, id -> new LinkedList<>()));
        }

    }

    public static void createAutoBadges(Identifier powerId, PowerType<?> powerType, List<Badge> badgeList) {

        Power power = powerType.create(null);

        if (power instanceof Active active) {

            boolean toggle = active instanceof TogglePower || active instanceof ToggleNightVisionPower;
            Identifier autoBadgeId = toggle ? TOGGLE_BADGE_ID : ACTIVE_BADGE_ID;

            if (REGISTRY.containsId(autoBadgeId)) {
                badgeList.add(REGISTRY.get(autoBadgeId));
            } else {

                Identifier spriteId = toggle ? TOGGLE_BADGE_SPRITE : ACTIVE_BADGE_SPRITE;
                String key = toggle ? "origins.gui.badge.toggle" : "origins.gui.badge.active";

                badgeList.add(new KeybindBadge(spriteId, key));

            }

        } else if (power instanceof RecipePower recipePower) {

            RecipeEntry<Recipe<CraftingInventory>> entry = recipePower.getRecipe();
            String type = (Recipe<?>) entry.value() instanceof ShapedRecipe ? "shaped" : "shapeless";

            badgeList.add(new CraftingRecipeBadge(
                RECIPE_BADGE_SPRITE, entry, Text.translatable("origins.gui.badge.recipe.crafting." + type), null
            ));

        }

    }
}
