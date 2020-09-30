package io.github.apace100.origins.power.factory.condition.player;

import io.github.apace100.origins.Origins;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class IsInvisibleCondition extends PlayerCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "is_invisible");

    @Override
    public boolean isFulfilled(PlayerEntity playerEntity) {
        return playerEntity.isInvisible();
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }
}
