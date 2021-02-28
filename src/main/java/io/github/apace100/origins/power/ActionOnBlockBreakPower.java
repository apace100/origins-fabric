package io.github.apace100.origins.power;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnBlockBreakPower extends Power {

    private final Predicate<CachedBlockPosition> predicate;
    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;
    private boolean onlyWhenHarvested;

    public ActionOnBlockBreakPower(PowerType<?> type, PlayerEntity player, Predicate<CachedBlockPosition> predicate, Consumer<Entity> entityAction, Consumer<Triple<World, BlockPos, Direction>> blockAction, boolean onlyWhenHarvested) {
        super(type, player);
        this.predicate = predicate;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
        this.onlyWhenHarvested = onlyWhenHarvested;
    }

    public boolean doesApply(BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(player.world, pos, true);
        return doesApply(cbp);
    }

    public boolean doesApply(CachedBlockPosition pos) {
        return predicate.test(pos);
    }

    public void executeActions(boolean successfulHarvest, BlockPos pos, Direction dir) {
        if(successfulHarvest || !onlyWhenHarvested) {
            if(blockAction != null) {
                blockAction.accept(Triple.of(player.world, pos, dir));
            }
            if(entityAction != null) {
                entityAction.accept(player);
            }
        }
    }
}
