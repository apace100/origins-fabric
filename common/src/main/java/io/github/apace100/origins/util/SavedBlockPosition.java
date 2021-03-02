package io.github.apace100.origins.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class SavedBlockPosition extends CachedBlockPosition {

    private final BlockState blockState;
    private final BlockEntity blockEntity;

    public SavedBlockPosition(WorldView world, BlockPos pos) {
        super(world, pos, true);
        this.blockState = world.getBlockState(pos);
        this.blockEntity = world.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState() {
        return blockState;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public WorldView getWorld() {
        return super.getWorld();
    }

    @Override
    public BlockPos getBlockPos() {
        return super.getBlockPos();
    }
}
