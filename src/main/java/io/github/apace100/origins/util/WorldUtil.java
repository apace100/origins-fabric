package io.github.apace100.origins.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public final class WorldUtil {

    public static boolean isRainingAtPlayerPosition(PlayerEntity player) {
        BlockPos blockPos = player.getBlockPos();
        return player.world.hasRain(blockPos) || player.world.hasRain(blockPos.add(0.0D, player.getDimensions(player.getPose()).height, 0.0D));
    }
}
