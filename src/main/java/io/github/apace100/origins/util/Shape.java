package io.github.apace100.origins.util;

import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum Shape {
    CUBE, STAR;

    public static Collection<BlockPos> getPositions(BlockPos center, Shape shape, int radius) {
        Set<BlockPos> positions = new HashSet<>();
        for(int i = -radius; i <= radius; i++) {
            for(int j = -radius; j <= radius; j++) {
                for(int k = -radius; k <= radius; k++) {
                    if(shape == Shape.CUBE || (Math.abs(i) + Math.abs(j) + Math.abs(k)) <= radius) {
                        positions.add(new BlockPos(center.add(i, j, k)));
                    }
                }
            }
        }
        return positions;
    }
}
