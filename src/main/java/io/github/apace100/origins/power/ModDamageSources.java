package io.github.apace100.origins.power;

import io.github.apace100.origins.mixin.DamageSourceAccessor;
import net.minecraft.entity.damage.DamageSource;

public class ModDamageSources {

    public static final DamageSource NO_WATER_FOR_GILLS = ((DamageSourceAccessor)((DamageSourceAccessor)DamageSourceAccessor.createDamageSource("no_water_for_gills")).callSetBypassesArmor()).callSetUnblockable();
    public static final DamageSource HURT_BY_WATER = ((DamageSourceAccessor)((DamageSourceAccessor)DamageSourceAccessor.createDamageSource("hurt_by_water")).callSetBypassesArmor()).callSetUnblockable();
}
