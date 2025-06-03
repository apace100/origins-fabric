package io.github.apace100.origins.util;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.type.Active;
import io.github.apace100.apoli.util.keybinding.KeyBindingReference;
import io.github.apace100.apoli.util.keybinding.KeyBindingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;

public class PowerKeyManager {

    private static final Map<Identifier, String> KEY_CACHE = new Object2ObjectOpenHashMap<>();

    public static void clearCache() {
        KEY_CACHE.clear();
    }

    public static Optional<String> getKeyId(Power power) {

        Identifier powerId = power.getId();
        if (KEY_CACHE.containsKey(powerId)) {
            return Optional.ofNullable(KEY_CACHE.get(powerId));
        }

        else {
            return getKeyFromPower(power).map(str -> KEY_CACHE.computeIfAbsent(powerId, id -> str));
        }

    }

    private static Optional<String> getKeyFromPower(Power power) {

        if (power.getType() instanceof Active activePowerType) {

            KeyBindingReference keyBindingReference = activePowerType.getKey();
            String keyId = keyBindingReference.id();

            if (KeyBindingUtil.ALIASES.hasAlias(keyId)) {
                keyId = KeyBindingUtil.ALIASES.resolveAlias(keyId);
            }

            return Optional.of(keyId);

        }

        else {
            return Optional.empty();
        }

    }

}
