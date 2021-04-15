package io.github.apace100.origins.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

public class EntityGlowPower extends Power {

    private final Predicate<LivingEntity> entityCondition;

    public EntityGlowPower(PowerType<?> type, PlayerEntity player, Predicate<LivingEntity> entityCondition) {
        super(type, player);
        this.entityCondition = entityCondition;
    }

    public boolean doesApply(Entity e) {
        return e instanceof LivingEntity && (entityCondition == null || entityCondition.test((LivingEntity)e));
    }
}
