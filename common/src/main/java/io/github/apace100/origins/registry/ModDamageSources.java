package io.github.apace100.origins.registry;

import net.minecraft.entity.damage.DamageSource;

public class ModDamageSources {

    public static final DamageSource NO_WATER_FOR_GILLS = new DamageSource("no_water_for_gills").setBypassesArmor().setUnblockable();
    public static final DamageSource GENERIC_DOT = new DamageSource("genericDamageOverTime").setBypassesArmor().setUnblockable();
}
