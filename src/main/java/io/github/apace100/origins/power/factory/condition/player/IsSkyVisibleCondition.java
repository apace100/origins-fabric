package io.github.apace100.origins.power.factory.condition.player;

import io.github.apace100.origins.Origins;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class IsSkyVisibleCondition extends PlayerCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "is_sky_visible");

    @Override
    public boolean isFulfilled(PlayerEntity playerEntity) {
        BlockPos blockPos = playerEntity.getVehicle() instanceof BoatEntity ? (new BlockPos(playerEntity.getX(), (double) Math.round(playerEntity.getY()), playerEntity.getZ())).up() : new BlockPos(playerEntity.getX(), (double) Math.round(playerEntity.getY()), playerEntity.getZ());
        return playerEntity.world.isSkyVisible(blockPos);
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }
}
