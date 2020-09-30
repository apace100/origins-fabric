package io.github.apace100.origins.power.factory.condition.damage;

import io.github.apace100.origins.Origins;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class IsFireCondition extends DamageCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "is_fire");

    @Override
    protected boolean isFulfilled(Pair<DamageSource, Float> damage) {
        return damage.getLeft().isFire();
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }
}
