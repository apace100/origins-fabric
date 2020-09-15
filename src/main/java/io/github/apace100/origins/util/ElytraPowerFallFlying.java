package io.github.apace100.origins.util;

import io.github.apace100.origins.power.PowerTypes;
import net.adriantodt.fallflyinglib.FallFlyingAbility;
import net.minecraft.entity.LivingEntity;

public class ElytraPowerFallFlying implements FallFlyingAbility {

    private final LivingEntity entity;

    public ElytraPowerFallFlying(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean allowFallFlying() {
        return PowerTypes.ELYTRA.isActive(entity);
    }

    @Override
    public boolean shouldHideCape() {
        return false;
    }
}
