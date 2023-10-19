package io.github.apace100.origins.component;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.util.ChoseOriginCriterion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        if (selectingOrigin) {
            invulnerabilityTicks = 60;
        }
    }

    @Override
    public boolean hasAllOrigins() {
        return OriginLayers.getLayers()
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
        return origins.get(layer);
    }

    @Override
    public boolean hadOriginBefore() {
        return hadOriginBefore;
    }

    @Override
    public void removeLayer(OriginLayer layer) {

        Origin oldOrigin = getOrigin(layer);
        if (oldOrigin != null) {
            PowerHolderComponent.KEY.get(player).removeAllPowersFromSource(oldOrigin.getIdentifier());
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
        if (oldOrigin != null) {

            if (!oldOrigin.getIdentifier().equals(origin.getIdentifier())) {
                powerComponent.removeAllPowersFromSource(oldOrigin.getIdentifier());
            }

            else if (!oldOrigin.toJson().equals(origin.toJson())) {
                revokeRemovedPowers(origin, powerComponent);
            }

        }

        grantPowersFromOrigin(origin, powerComponent);
        this.origins.put(layer, origin);

        if (this.hasAllOrigins()) {
            this.hadOriginBefore = true;
        }

        if (player instanceof ServerPlayerEntity spe) {
            ChoseOriginCriterion.INSTANCE.trigger(spe, origin);
        }

    }

    private void grantPowersFromOrigin(Origin origin, PowerHolderComponent powerComponent) {
        Identifier sourceId = origin.getIdentifier();
        origin.getPowerTypes()
            .stream()
            .filter(pt -> !powerComponent.hasPower(pt, sourceId))
            .forEach(pt -> powerComponent.addPower(pt, sourceId));
    }

    private void revokeRemovedPowers(Origin origin, PowerHolderComponent powerComponent) {
        Identifier sourceId = origin.getIdentifier();
        powerComponent.getPowersFromSource(sourceId)
            .stream()
            .filter(pt -> !origin.hasPowerType(pt))
            .forEach(pt -> powerComponent.removePower(pt, sourceId));
    }

    @Override
    public void tick() {
        if (!selectingOrigin && invulnerabilityTicks > 0) {
            invulnerabilityTicks--;
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound compoundTag) {

        if (player == null) {
            Origins.LOGGER.error("Player was null in PlayerOriginComponent#fromTag! This is not supposed to happen D:");
            return;
        }

        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
        origins.clear();

        //  Migrate origin data from old versions
        if (compoundTag.contains("Origin")) {
            try {

                OriginLayer defaultOriginLayer = OriginLayers.getLayer(Origins.identifier("origin"));
                origins.put(defaultOriginLayer, OriginRegistry.get(new Identifier(compoundTag.getString("Origin"))));

            } catch (Exception ignored) {
                Origins.LOGGER.warn("Player {} had old origin which could not be migrated: {}", player.getName().getString(), compoundTag.getString("Origin"));
            }
        } else {

            NbtList originLayersNbt = compoundTag.getList("OriginLayers", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < originLayersNbt.size(); i++) {

                NbtCompound originLayerNbt = originLayersNbt.getCompound(i);
                try {

                    Identifier layerId = new Identifier(originLayerNbt.getString("Layer"));
                    Identifier originId = new Identifier(originLayerNbt.getString("Origin"));

                    OriginLayer layer = OriginLayers.getLayer(layerId);
                    Origin origin = OriginRegistry.get(originId);

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
            grantPowersFromOrigin(origin, powerComponent);
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

                Identifier legacyPowerId = new Identifier(legacyPowerString);
                PowerType<?> legacyPowerType = PowerTypeRegistry.get(legacyPowerId);

                if (!powerComponent.hasPower(legacyPowerType)) {
                    continue;
                }

                try {
                    NbtElement legacyPowerData = legacyPowerNbt.get("Data");
                    powerComponent.getPower(legacyPowerType).fromTag(legacyPowerData);
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
    public void onPowersRead() {
        // NO-OP
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound compoundTag) {

        NbtList originLayersNbt = new NbtList();
        origins.forEach((layer, origin) -> {

            NbtCompound originLayerNbt = new NbtCompound();

            originLayerNbt.putString("Layer", layer.getIdentifier().toString());
            originLayerNbt.putString("Origin", origin.getIdentifier().toString());

            originLayersNbt.add(originLayerNbt);

        });

        compoundTag.put("OriginLayers", originLayersNbt);
        compoundTag.putBoolean("SelectingOrigin", selectingOrigin);
        compoundTag.putBoolean("HadOriginBefore", hadOriginBefore);

    }

    @Override
    public void sync() {
        OriginComponent.sync(this.player);
    }

}
