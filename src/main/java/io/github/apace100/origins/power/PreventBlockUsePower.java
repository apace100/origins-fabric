package io.github.apace100.origins.power;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.function.Predicate;

public class PreventBlockUsePower extends Power {

    private final Predicate<CachedBlockPosition> predicate;

    public PreventBlockUsePower(PowerType<?> type, PlayerEntity player, Predicate<CachedBlockPosition> predicate) {
        super(type, player);
        this.predicate = predicate;
    }

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(world, pos, true);
        return predicate.test(cbp);
    }
}
