package io.github.apace100.origins.util;

import io.github.apace100.apoli.power.*;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;

public class PowerKeyManager {

    private static final HashMap<Identifier, String> KEY_CACHE = new HashMap<>();

    public static void clearCache() {
        KEY_CACHE.clear();
    }

    public static String getKeyIdentifier(Identifier powerId) {
        if(KEY_CACHE.containsKey(powerId)) {
            return KEY_CACHE.get(powerId);
        }
        String key = getKeyFromPower(powerId);
        KEY_CACHE.put(powerId, key);
        return key;
    }

    private static String getKeyFromPower(Identifier powerId) {
        if(PowerTypeRegistry.contains(powerId)) {
            PowerType<?> powerType = PowerTypeRegistry.get(powerId);
            Power power = powerType.create(null);
            String key = "";
            if (power instanceof Active) {
                key = ((Active) power).getKey().key;
            } else if (powerType instanceof MultiplePowerType<?>) {
                List<Identifier> subs = ((MultiplePowerType<?>) powerType).getSubPowers();
                for (Identifier sub : subs) {
                    String subKey = getKeyFromPower(sub);
                    if (!subKey.isEmpty()) {
                        return subKey;
                    }
                }
            }
            return key.equals("none") ? "key.origins.primary_active" : key;
        }
        return "";
    }
}
