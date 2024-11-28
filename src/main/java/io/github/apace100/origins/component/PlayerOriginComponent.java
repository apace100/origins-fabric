package io.github.apace100.origins.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.*;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.util.ChoseOriginCriterion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class PlayerOriginComponent implements OriginComponent {

    private final Map<OriginLayer, Origin> origins = new ConcurrentHashMap<>();
    private final PlayerEntity player;

    private boolean selectingOrigin = false;
    private boolean hadOriginBefore = false;

    private int invulnerabilityTicks = 0;

    public PlayerOriginComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public boolean hasSelectionInvulnerability() {
        return invulnerabilityTicks > 0;
    }

    @Override
    public boolean isSelectingOrigin() {
        return selectingOrigin;
    }

    @Override
    public void selectingOrigin(boolean selectingOrigin) {

        this.selectingOrigin = selectingOrigin;

        if (selectingOrigin && !player.getWorld().isClient) {
            invulnerabilityTicks = 60;
        }

    }

    @Override
    public boolean hasAllOrigins() {
        return OriginLayerManager.values()
            .stream()
            .allMatch(layer -> !layer.isEnabled()
                            || (layer.getOrigins().isEmpty() || layer.getOriginOptionCount(player) == 0)
                            || hasOrigin(layer));
    }

    @Override
    public Map<OriginLayer, Origin> getOrigins() {
        return origins;
    }

    @Override
    public boolean hasOrigin(OriginLayer layer) {
        return origins.containsKey(layer)
            && origins.get(layer) != Origin.EMPTY;
    }

    @Override
    public Origin getOrigin(OriginLayer layer) {

        if (OriginLayerManager.contains(layer)) {
            return origins.computeIfAbsent(layer, k -> Origin.EMPTY);
        }

        else {
            return origins.getOrDefault(layer, Origin.EMPTY);
        }

    }

    @Override
    public boolean hadOriginBefore() {
        return hadOriginBefore;
    }

    @Override
    public void removeLayer(OriginLayer layer) {

        Origin oldOrigin = getOrigin(layer);
        if (oldOrigin != null) {
            PowerHolderComponent.KEY.get(player).removeAllPowersFromSource(oldOrigin.getId());
        }

        origins.remove(layer);

    }

    @Override
    public void setOrigin(OriginLayer layer, Origin origin) {

        Origin oldOrigin = getOrigin(layer);
        if (origin == oldOrigin) {
            return;
        }

        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
        RegistryOps<JsonElement> jsonOps = player.getRegistryManager().getOps(JsonOps.INSTANCE);

        if (oldOrigin != null) {

            JsonElement oldOriginJson = Origin.DATA_TYPE.write(jsonOps, oldOrigin).getOrThrow(JsonParseException::new);
            JsonElement originJson = Origin.DATA_TYPE.write(jsonOps, origin).getOrThrow(JsonParseException::new);

            if (!oldOrigin.getId().equals(origin.getId())) {
                PowerHolderComponent.revokeAllPowersFromSource(player, oldOrigin.getId(), true);
            }

            else if (!oldOriginJson.equals(originJson)) {
                revokeRemovedPowers(origin, powerComponent);
            }

        }

        grantPowersFromOrigin(origin);
        this.origins.put(layer, origin);

        if (this.hasAllOrigins()) {
            this.hadOriginBefore = true;
        }

        if (player instanceof ServerPlayerEntity spe) {
            ChoseOriginCriterion.INSTANCE.trigger(spe, origin);
        }

    }

    private void grantPowersFromOrigin(Origin origin) {
        PowerHolderComponent.grantPowers(this.player, Map.of(origin.getId(), origin.getPowers()), true);
    }

    private void revokeRemovedPowers(Origin origin, PowerHolderComponent powerComponent) {

        Identifier sourceId = origin.getId();
        List<Power> powers = powerComponent.getPowersFromSource(sourceId)
            .stream()
            .filter(Predicate.not(origin::hasPower))
            .toList();

        boolean revoked = powers
            .stream()
            .map(pt -> powerComponent.removePower(pt, sourceId))
            .reduce(false, Boolean::logicalOr);

        if (revoked) {
            PowerHolderComponent.PacketHandlers.REVOKE_POWERS.sync(player, Map.of(sourceId, powers));
        }

    }

    @Override
    public void serverTick() {
        if (!selectingOrigin && invulnerabilityTicks > 0) {
            invulnerabilityTicks--;
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound compoundTag, RegistryWrapper.WrapperLookup wrapperLookup) {

        if (player == null) {
            Origins.LOGGER.error("Player was null in PlayerOriginComponent#fromTag! This is not supposed to happen D:");
            return;
        }

        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
        origins.clear();

        //  Migrate origin data from old versions
        if (compoundTag.contains("Origin")) {
            try {

                OriginLayer defaultOriginLayer = OriginLayerManager.get(Origins.identifier("origin"));
                origins.put(defaultOriginLayer, OriginManager.get(Identifier.of(compoundTag.getString("Origin"))));

            } catch (Exception ignored) {
                Origins.LOGGER.warn("Player {} had old origin which could not be migrated: {}", player.getName().getString(), compoundTag.getString("Origin"));
            }
        } else {

            NbtList originLayersNbt = compoundTag.getList("OriginLayers", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < originLayersNbt.size(); i++) {

                NbtCompound originLayerNbt = originLayersNbt.getCompound(i);
                try {

                    Identifier layerId = Identifier.of(originLayerNbt.getString("Layer"));
                    Identifier originId = Identifier.of(originLayerNbt.getString("Origin"));

                    OriginLayer layer = OriginLayerManager.get(layerId);
                    Origin origin = OriginManager.get(originId);

                    origins.put(layer, origin);

                    if (layer.contains(origin) || origin.isSpecial()) {
                        continue;
                    }

                    Origins.LOGGER.warn("Origin \"{}\" is not in origin layer \"{}\" and is not considered special, but was found on player {}!", originId, layerId, player.getName().getString());

                    powerComponent.removeAllPowersFromSource(originId);
                    origins.put(layer, Origin.EMPTY);

                } catch (Exception e) {
                    Origins.LOGGER.error("There was a problem trying to read origin NBT data of player {}: {}", player.getName().getString(), e.getMessage());
                }

            }

        }

        selectingOrigin = compoundTag.getBoolean("SelectingOrigin");
        hadOriginBefore = compoundTag.getBoolean("HadOriginBefore");

        if (player.getWorld().isClient) {
            return;
        }

        for (Origin origin : origins.values()) {
            //  Grant powers only if the player doesn't have them yet from the specific Origin source.
            //  Needed in case the origin was set before the update to Apoli happened.
            grantPowersFromOrigin(origin);
        }

        for (Origin origin : origins.values()) {
            revokeRemovedPowers(origin, powerComponent);
        }

        //  Compatibility with old worlds. Load power data from Origins' NBT, whereas in new versions, power data is
        //  stored in Apoli's NBT
        if (!compoundTag.contains("Powers")) {
            return;
        }

        NbtList legacyPowersNbt = compoundTag.getList("Powers", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < legacyPowersNbt.size(); i++) {

            NbtCompound legacyPowerNbt = legacyPowersNbt.getCompound(i);
            String legacyPowerString = legacyPowerNbt.getString("Type");

            try {

                Identifier legacyPowerId = Identifier.of(legacyPowerString);
                Power legacyPower = PowerManager.get(legacyPowerId);

                if (!powerComponent.hasPower(legacyPower)) {
                    continue;
                }

                try {
                    NbtElement legacyPowerData = legacyPowerNbt.get("Data");
                    powerComponent.getPowerType(legacyPower).fromTag(legacyPowerData);
                } catch (ClassCastException e) {
                    //  Occurs when the power was overridden by a data pack since last world load
                    //  where the overridden power now uses different data classes
                    Origins.LOGGER.warn("Data type of power \"{}\" changed, skipping data for that power on entity {}", legacyPowerId, player.getName().getString());
                }


            } catch (IllegalArgumentException e) {
                Origins.LOGGER.warn("Power data of unregistered power \"{}\" found on player {}, skipping...", legacyPowerString, player.getName().getString());
            }

        }

    }

    @Override
    public void writeToNbt(@NotNull NbtCompound compoundTag, RegistryWrapper.WrapperLookup wrapperLookup) {

        NbtList originLayersNbt = new NbtList();
        origins.forEach((layer, origin) -> {

            NbtCompound originLayerNbt = new NbtCompound();

            originLayerNbt.putString("Layer", layer.getId().toString());
            originLayerNbt.putString("Origin", origin.getId().toString());

            originLayersNbt.add(originLayerNbt);

        });

        compoundTag.put("OriginLayers", originLayersNbt);
        compoundTag.putBoolean("SelectingOrigin", selectingOrigin);
        compoundTag.putBoolean("HadOriginBefore", hadOriginBefore);

    }

    @Override
    public void sync() {
        ModComponents.ORIGIN.sync(player);
    }

}
