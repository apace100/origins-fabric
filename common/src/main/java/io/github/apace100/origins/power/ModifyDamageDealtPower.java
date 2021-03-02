package io.github.apace100.origins.power;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class ModifyDamageDealtPower extends ValueModifyingPower {

    private final Predicate<Pair<DamageSource, Float>> condition;

    public ModifyDamageDealtPower(PowerType<?> type, PlayerEntity player, Predicate<Pair<DamageSource, Float>> condition) {
        super(type, player);
        this.condition = condition;
    }

    public boolean doesApply(DamageSource source, float damageAmount) {
        return condition.test(new Pair<>(source, damageAmount));
    }
}
