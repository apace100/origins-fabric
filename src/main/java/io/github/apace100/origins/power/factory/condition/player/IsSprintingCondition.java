package io.github.apace100.origins.power.factory.condition.player;

import io.github.apace100.origins.Origins;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class IsSprintingCondition extends PlayerCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "is_sprinting");

    @Override
    public boolean isFulfilled(PlayerEntity playerEntity) {
        return playerEntity.isSprinting();
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }
}
