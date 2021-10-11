package io.github.apace100.origins.screen;

import com.google.common.collect.Lists;
import io.github.apace100.apoli.integration.PostPowerLoadCallback;
import io.github.apace100.apoli.integration.PowerReloadCallback;
import io.github.apace100.apoli.power.*;
import io.github.apace100.origins.Origins;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BadgeManager {

    private final HashMap<Identifier, List<Badge>> badges = new HashMap<>();

    public BadgeManager() {
        PowerReloadCallback.EVENT.register(this::clear);
        PowerTypes.registerAdditionalData("badges", (powerId, factoryId, isSubPower, data, powerType) -> {
            if(!powerType.isHidden()) {
                if(data.isJsonArray()) {
                    badges.put(powerId, new LinkedList<>());
                    data.getAsJsonArray().forEach(badgeJson -> {
                        if(badgeJson.isJsonObject()) {
                            Badge badge = Badge.fromData(Badge.DATA.read(badgeJson.getAsJsonObject()));
                            addBadge(powerId, badge);
                        } else {
                            Origins.LOGGER.error("\"badges\" field in power \"" + powerId
                                + "\" contained an entry that was not a JSON object.");
                        }
                    });
                } else {
                    Origins.LOGGER.error("\"badges\" field in power \"" + powerId + "\" should be an array.");
                }
            }
        });
        PostPowerLoadCallback.EVENT.register((powerId, factoryId, isSubPower, data, powerType) -> {
            if(!badges.containsKey(powerId) || badges.get(powerId).size() == 0) {
                Power power = powerType.create(null);
                Badge autoBadge = null;
                if(power instanceof TogglePower || power instanceof ToggleNightVisionPower) {
                    autoBadge = Badge.TOGGLE;
                } else if(power instanceof Active) {
                    autoBadge = Badge.ACTIVE;
                }
                if(autoBadge == null) {
                    if(powerType instanceof MultiplePowerType<?> mp) {
                        for(Identifier subPower : mp.getSubPowers()) {
                            if(PowerTypeRegistry.contains(subPower)) {
                                Power sp = PowerTypeRegistry.get(subPower).create(null);
                                if(sp instanceof TogglePower || sp instanceof ToggleNightVisionPower) {
                                    autoBadge = Badge.TOGGLE;
                                    break;
                                } else if(sp instanceof Active) {
                                    autoBadge = Badge.ACTIVE;
                                    break;
                                }
                            }
                        }
                    }
                }
                if(autoBadge != null) {
                    addBadge(powerId, autoBadge);
                }
            }
        });
    }

    public void clear() {
        badges.clear();
    }

    public void addBadge(Identifier powerId, Badge badge) {
        List<Badge> badgeList = badges.computeIfAbsent(powerId, id -> new LinkedList<>());
        badgeList.add(badge);
    }

    public List<Badge> getBadges(Identifier powerId) {
        if(!badges.containsKey(powerId)) {
            return Lists.newArrayList();
        }
        return badges.get(powerId);
    }

    public void writeSyncData(PacketByteBuf buf) {
        buf.writeInt(badges.size());
        badges.forEach((id, list) -> {
            buf.writeIdentifier(id);
            buf.writeInt(list.size());
            list.forEach(badge -> {
                Badge.DATA.write(buf, badge.getData());
            });
        });
    }
}
