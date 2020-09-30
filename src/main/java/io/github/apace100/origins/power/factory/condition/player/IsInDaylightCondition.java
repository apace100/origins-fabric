package io.github.apace100.origins.power.factory.condition.player;

import io.github.apace100.origins.Origins;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class IsInDaylightCondition extends PlayerCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "is_in_daylight");

    @Override
    public boolean isFulfilled(PlayerEntity playerEntity) {
        if (playerEntity.world.isDay() && !this.isRainingAtPlayerPosition(playerEntity)) {
            float f = playerEntity.getBrightnessAtEyes();
            BlockPos blockPos = playerEntity.getVehicle() instanceof BoatEntity ? (new BlockPos(playerEntity.getX(), (double) Math.round(playerEntity.getY()), playerEntity.getZ())).up() : new BlockPos(playerEntity.getX(), (double) Math.round(playerEntity.getY()), playerEntity.getZ());
            if (f > 0.5F && playerEntity.world.isSkyVisible(blockPos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRainingAtPlayerPosition(PlayerEntity player) {
        BlockPos blockPos = player.getBlockPos();
        return player.world.hasRain(blockPos) || player.world.hasRain(blockPos.add(0.0D, player.getDimensions(player.getPose()).height, 0.0D));
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }
}
