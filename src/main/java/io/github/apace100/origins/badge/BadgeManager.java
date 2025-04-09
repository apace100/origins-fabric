package io.github.apace100.origins.badge;

import com.google.gson.*;
import io.github.apace100.apoli.integration.PostPowerLoadCallback;
import io.github.apace100.apoli.integration.PowerOverrideCallback;
import io.github.apace100.apoli.integration.PrePowerReloadCallback;
import io.github.apace100.apoli.power.MultiplePower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.Active;
import io.github.apace100.apoli.power.type.RecipePowerType;
import io.github.apace100.apoli.power.type.ToggleNightVisionPowerType;
import io.github.apace100.apoli.power.type.TogglePowerType;
import io.github.apace100.calio.data.DataException;
import io.github.apace100.calio.registry.DataObjectRegistry;
import io.github.apace100.calio.util.DynamicIdentifier;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.integration.AutoBadgeCallback;
import io.github.apace100.origins.networking.packet.s2c.SyncBadgesS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class BadgeManager {

    public static final DataObjectRegistry<Badge> REGISTRY = new DataObjectRegistry.Builder<Badge>(Origins.identifier("badge"))
        .readFromData("badges", true)
        .dataErrorHandler((id, packName, exception) -> Origins.LOGGER.error("Error trying to read badge \"" + id + "\" from data pack [" + packName + "]: " + exception))
        .defaultFactory(BadgeFactories.KEYBIND)
        .buildAndRegister();

    private static final Map<Identifier, List<Badge>> BADGES_BY_ID = new HashMap<>();

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
        PrePowerReloadCallback.EVENT.register(BADGES_BY_ID::clear);
        PowerManager.registerAdditionalData("badges", BadgeManager::readCustomBadges);
        PowerOverrideCallback.EVENT.register(BADGES_BY_ID::remove);
        PostPowerLoadCallback.EVENT.register(BadgeManager::readAutoBadges);
        AutoBadgeCallback.EVENT.register(BadgeManager::createAutoBadges);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(PowerManager.ID, REGISTRY.getRegistryId());
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(REGISTRY.getRegistryId(), (player, joined) -> send(player));
    }

    public static void send(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new SyncBadgesS2CPacket(BADGES_BY_ID));
    }

    @Environment(EnvType.CLIENT)
    public static void receive(SyncBadgesS2CPacket packet, ClientPlayNetworking.Context context) {
        BADGES_BY_ID.clear();
        BADGES_BY_ID.putAll(packet.badgesById());
    }

    public static void register(BadgeFactory factory) {
        REGISTRY.registerFactory(factory.id(), factory);
    }

    public static List<Badge> getPowerBadges(Identifier powerId) {
        return BADGES_BY_ID.getOrDefault(powerId, List.of());
    }

    public static boolean hasPowerBadges(Identifier powerId) {
        return BADGES_BY_ID.containsKey(powerId);
    }

    public static boolean hasPowerBadges(Power power) {
        return hasPowerBadges(power.getId());
    }

    private static void readCustomBadges(Identifier powerId, Identifier factoryId, boolean isSubPower, JsonElement data, Power power) {

        if (power.isHidden() || isSubPower) {
            return;
        }

        try {

            if (!(data instanceof JsonArray dataArray)) {
                throw new JsonSyntaxException("Not a JSON array: " + data);
            }

            List<Badge> badges = BADGES_BY_ID.computeIfAbsent(powerId, id -> new LinkedList<>());
            for (int i = 0; i < dataArray.size(); i++) {

                JsonElement badgeJson = dataArray.get(i);
                Badge badge = switch (badgeJson) {
                    case JsonObject jsonObject -> {

                        try {
                            yield REGISTRY.readDataObject(jsonObject);
                        }

                        catch (DataException de) {
                            throw de.prependArray(i);
                        }

                        catch (Exception e) {
                            throw new DataException(DataException.Phase.READING, i, e);
                        }

                    }
                    case JsonPrimitive jsonPrimitive -> {

                        Identifier badgeId = DynamicIdentifier.of(jsonPrimitive);
                        Badge referencedBadge = REGISTRY.get(badgeId);

                        if (referencedBadge != null) {
                            yield referencedBadge;
                        }

                        else {
                            throw new DataException(DataException.Phase.READING, i, "Badge \"" + badgeId + "\" is undefined!");
                        }

                    }
                    default ->
                        throw new JsonSyntaxException("Not a JSON object or string: " + badgeJson);
                };

                badges.add(badge);

            }

        }

        catch (Exception e) {
            Origins.LOGGER.error("There was a problem reading badges of power \"{}\": {}", powerId, (e.getMessage() != null ? e.getMessage() : e));
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
    private static void readAutoBadges(Identifier powerId, Identifier factoryId, boolean isSubPower, JsonObject json, Power power) {

        if (!hasPowerBadges(powerId) && !(power instanceof MultiplePower) && (isSubPower || !power.isHidden())) {
            AutoBadgeCallback.EVENT
                .invoker()
                .createAutoBadge(powerId, power, BADGES_BY_ID.computeIfAbsent(powerId, id -> new LinkedList<>()));
        }

    }

    private static void createAutoBadges(Identifier powerId, Power power, List<Badge> badgeList) {

        switch (power.getType()) {
            case Active active -> {

                boolean toggle = active instanceof TogglePowerType
                    || active instanceof ToggleNightVisionPowerType;
                Identifier autoBadgeId = toggle
                    ? TOGGLE_BADGE_ID
                    : ACTIVE_BADGE_ID;

                if (REGISTRY.containsId(autoBadgeId)) {
                    badgeList.add(REGISTRY.get(autoBadgeId));
                }

                else {

                    Identifier spriteId = toggle
                        ? TOGGLE_BADGE_SPRITE
                        : ACTIVE_BADGE_SPRITE;
                    String key = toggle
                        ? "origins.gui.badge.toggle"
                        : "origins.gui.badge.active";

                    badgeList.add(new KeybindBadge(spriteId, key));

                }

            }
            case RecipePowerType recipePowerType -> {

                CraftingRecipe craftingRecipe = recipePowerType.getRecipe();
                String type = switch (craftingRecipe) {
                    case ShapedRecipe ignored ->
                        "shaped";
                    case ShapelessRecipe ignored ->
                        "shapeless";
                    default ->
                        "unknown";
                };

                badgeList.add(new CraftingRecipeBadge(
                    RECIPE_BADGE_SPRITE,
                    new RecipeEntry<>(powerId, craftingRecipe),
                    Text.translatable("origins.gui.badge.recipe.crafting." + type),
                    null
                ));

            }
            default -> {

            }
        }

    }

}
