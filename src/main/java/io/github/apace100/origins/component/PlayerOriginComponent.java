package io.github.apace100.origins.component;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PlayerOriginComponent implements OriginComponent {

    private PlayerEntity player;
    private HashMap<OriginLayer, Origin> origins = new HashMap<>();

    private boolean hadOriginBefore = false;

    public PlayerOriginComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public boolean hasAllOrigins() {
        return OriginLayers.getLayers().stream().allMatch(layer -> {
            return !layer.isEnabled() || layer.getOrigins(player).size() == 0 || (origins.containsKey(layer) && origins.get(layer) != null && origins.get(layer) != Origin.EMPTY);
        });
    }

    @Override
    public HashMap<OriginLayer, Origin> getOrigins() {
        return origins;
    }

    @Override
    public boolean hasOrigin(OriginLayer layer) {
        return origins != null && origins.containsKey(layer) && origins.get(layer) != null && origins.get(layer) != Origin.EMPTY;
    }

    @Override
    public Origin getOrigin(OriginLayer layer) {
        if(!origins.containsKey(layer)) {
            return null;
        }
        return origins.get(layer);
    }

    @Override
    public boolean hadOriginBefore() {
        return hadOriginBefore;
    }

    @Override
    public void setOrigin(OriginLayer layer, Origin origin) {
        Origin oldOrigin = getOrigin(layer);
        if(oldOrigin == origin) {
            return;
        }
        this.origins.put(layer, origin);
        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
        for(PowerType<?> powerType : origin.getPowerTypes()) {
            powerComponent.addPower(powerType, origin.getIdentifier());
        }
        if(oldOrigin != null) {
            powerComponent.removeAllPowersFromSource(oldOrigin.getIdentifier());
        }
        if(this.hasAllOrigins()) {
            this.hadOriginBefore = true;
        }
    }

    @Override
    public void readFromNbt(NbtCompound compoundTag) {
        this.fromTag(compoundTag, true);
    }

    private void fromTag(NbtCompound compoundTag, boolean callPowerOnAdd) {

        if(player == null) {
            Origins.LOGGER.error("Player was null in `fromTag`! This is a bug!");
        }
        if(this.origins != null) {
            /*if(callPowerOnAdd) {
                for (Power power: powers.values()) {
                    power.onRemoved();
                    power.onLost();
                }
            }
            powers.clear();*/
        }

        this.origins.clear();

        if(compoundTag.contains("Origin")) {
            try {
                OriginLayer defaultOriginLayer = OriginLayers.getLayer(new Identifier(Origins.MODID, "origin"));
                this.origins.put(defaultOriginLayer, OriginRegistry.get(Identifier.tryParse(compoundTag.getString("Origin"))));
            } catch(IllegalArgumentException e) {
                Origins.LOGGER.warn("Player " + player.getDisplayName().asString() + " had old origin which could not be migrated: " + compoundTag.getString("Origin"));
            }
        } else {
            NbtList originLayerList = (NbtList)compoundTag.get("OriginLayers");
            if(originLayerList != null) {
                for(int i = 0; i < originLayerList.size(); i++) {
                    NbtCompound layerTag = originLayerList.getCompound(i);
                    Identifier layerId = Identifier.tryParse(layerTag.getString("Layer"));
                    OriginLayer layer = null;
                    try {
                        layer = OriginLayers.getLayer(layerId);
                    } catch(IllegalArgumentException e) {
                        Origins.LOGGER.warn("Could not find origin layer with id " + layerId.toString() + ", which existed on the data of player " + player.getDisplayName().asString() + ".");
                    }
                    if(layer != null) {
                        Identifier originId = Identifier.tryParse(layerTag.getString("Origin"));
                        Origin origin = null;
                        try {
                            origin = OriginRegistry.get(originId);
                        } catch(IllegalArgumentException e) {
                            Origins.LOGGER.warn("Could not find origin with id " + originId.toString() + ", which existed on the data of player " + player.getDisplayName().asString() + ".");
                        }
                        if(origin != null) {
                            if(!layer.contains(origin) && !origin.isSpecial()) {
                                Origins.LOGGER.warn("Origin with id " + origin.getIdentifier().toString() + " is not in layer " + layer.getIdentifier().toString() + " and is not special, but was found on " + player.getDisplayName().asString() + ", setting to EMPTY.");
                                origin = Origin.EMPTY;
                            }
                            this.origins.put(layer, origin);
                        }
                    }
                }
            }
        }
        this.hadOriginBefore = compoundTag.getBoolean("HadOriginBefore");
        /*
        NbtList powerList = (NbtList)compoundTag.get("Powers");
        for(int i = 0; i < powerList.size(); i++) {
            NbtCompound powerTag = powerList.getCompound(i);
            Identifier powerTypeId = Identifier.tryParse(powerTag.getString("Type"));
            try {
                PowerType<?> type = PowerTypeRegistry.get(powerTypeId);
                if(hasPowerType(type)) {
                    NbtElement data = powerTag.get("Data");
                    Power power = type.create(player);
                    try {
                        power.fromTag(data);
                    } catch(ClassCastException e) {
                        // Occurs when power was overriden by data pack since last world load
                        // to be a power type which uses different data class.
                        Origins.LOGGER.warn("Data type of \"" + powerTypeId + "\" changed, skipping data for that power on player " + player.getName().asString());
                    }
                    this.powers.put(type, power);
                    if(callPowerOnAdd) {
                        power.onAdded();
                    }
                }
            } catch(IllegalArgumentException e) {
                Origins.LOGGER.warn("Power data of unregistered power \"" + powerTypeId + "\" found on player, skipping...");
            }
        }*/
        /*
        this.getPowerTypes().forEach(pt -> {
            if(!this.powers.containsKey(pt)) {
                Power power = pt.create(player);
                this.powers.put(pt, power);
            }
        });*/
    }

    @Override
    public void writeToNbt(NbtCompound compoundTag) {
        NbtList originLayerList = new NbtList();
        for(Map.Entry<OriginLayer, Origin> entry : origins.entrySet()) {
            NbtCompound layerTag = new NbtCompound();
            layerTag.putString("Layer", entry.getKey().getIdentifier().toString());
            layerTag.putString("Origin", entry.getValue().getIdentifier().toString());
            originLayerList.add(layerTag);
        }
        compoundTag.put("OriginLayers", originLayerList);
        compoundTag.putBoolean("HadOriginBefore", this.hadOriginBefore);
        /*NbtList powerList = new NbtList();
        for(Map.Entry<PowerType<?>, Power> powerEntry : powers.entrySet()) {
            NbtCompound powerTag = new NbtCompound();
            powerTag.putString("Type", PowerTypeRegistry.getId(powerEntry.getKey()).toString());
            powerTag.put("Data", powerEntry.getValue().toTag());
            powerList.add(powerTag);
        }
        compoundTag.put("Powers", powerList);*/
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        NbtCompound compoundTag = buf.readNbt();
        if(compoundTag != null) {
            this.fromTag(compoundTag, false);
        }
    }

    @Override
    public void sync() {
        OriginComponent.sync(this.player);
    }
}
