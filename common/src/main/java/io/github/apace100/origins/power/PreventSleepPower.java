package io.github.apace100.origins.power;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.function.Predicate;

public class PreventSleepPower extends Power {

    private final Predicate<CachedBlockPosition> predicate;
    private final String message;
    private final boolean allowSpawnPoint;

    public PreventSleepPower(PowerType<?> type, PlayerEntity player, Predicate<CachedBlockPosition> predicate, String message, boolean allowSpawnPoint) {
        super(type, player);
        this.predicate = predicate;
        this.message = message;
        this.allowSpawnPoint = allowSpawnPoint;
    }

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(world, pos, true);
        return predicate.test(cbp);
    }

    public String getMessage() {
        return message;
    }

    public boolean doesAllowSpawnPoint() {
        return allowSpawnPoint;
    }
}
