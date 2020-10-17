package io.github.apace100.origins.registry;

import io.github.apace100.origins.mixin.DamageSourceAccessor;
import net.minecraft.entity.damage.DamageSource;

public class ModDamageSources {

    public static final DamageSource NO_WATER_FOR_GILLS = ((DamageSourceAccessor)((DamageSourceAccessor)DamageSourceAccessor.createDamageSource("no_water_for_gills")).callSetBypassesArmor()).callSetUnblockable();
    public static final DamageSource GENERIC_DOT = ((DamageSourceAccessor)((DamageSourceAccessor)DamageSourceAccessor.createDamageSource("genericDamageOverTime")).callSetBypassesArmor()).callSetUnblockable();
}
