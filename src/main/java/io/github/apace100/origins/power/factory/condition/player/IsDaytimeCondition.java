package io.github.apace100.origins.power.factory.condition.player;

import io.github.apace100.origins.Origins;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class IsDaytimeCondition extends PlayerCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "is_daytime");

    @Override
    public boolean isFulfilled(PlayerEntity playerEntity) {
        return playerEntity.world.isDay();
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }
}
