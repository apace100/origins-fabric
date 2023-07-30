package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.HashMap;
import java.util.Map;

public class ModDamageSources {

    public static final RegistryKey<DamageType> NO_WATER_FOR_GILLS = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Origins.identifier("no_water_for_gills"));

    private static final Map<RegistryKey<DamageType>, DamageSource> damageSourceCache = new HashMap<>();

    public static DamageSource getSource(DamageSources damageSources, RegistryKey<DamageType> damageType) {
        return damageSourceCache.computeIfAbsent(damageType, damageSources::create);
    }
}
