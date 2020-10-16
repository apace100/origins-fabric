package io.github.apace100.origins.power;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class ModifyHarvestPower extends Power {

    private final Predicate<CachedBlockPosition> predicate;
    private boolean allow;

    public ModifyHarvestPower(PowerType<?> type, PlayerEntity player, Predicate<CachedBlockPosition> predicate, boolean allow) {
        super(type, player);
        this.predicate = predicate;
        this.allow = allow;
    }

    public boolean doesApply(BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(player.world, pos, true);
        return predicate.test(cbp);
    }

    public boolean doesApply(CachedBlockPosition pos) {
        return predicate.test(pos);
    }

    public boolean isHarvestAllowed() {
        return allow;
    }
}
