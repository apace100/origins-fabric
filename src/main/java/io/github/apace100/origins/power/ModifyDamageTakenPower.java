package io.github.apace100.origins.power;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class ModifyDamageTakenPower extends FloatModifyingPower {

    private final Predicate<Pair<DamageSource, Float>> condition;

    public ModifyDamageTakenPower(PowerType<?> type, PlayerEntity player, Predicate<Pair<DamageSource, Float>> condition, EntityAttributeModifier modifier) {
        super(type, player, modifier);
        this.condition = condition;
    }

    public boolean doesApply(DamageSource source, float damageAmount) {
        return condition.test(new Pair(source, damageAmount));
    }
}
