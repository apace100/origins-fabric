package io.github.apace100.origins.content;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.tick.OrderedTick;

public class TemporaryCobwebBlock extends CobwebBlock {

	public TemporaryCobwebBlock(AbstractBlock.Settings settings) {
		super(settings);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if(!world.isClient) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.empty();
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.empty();
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		worldIn.scheduleBlockTick(pos, this, 60);
		super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		//if (PowerTypes.WEBBING.isActive(entityIn)) {
		//	return;
		//}
		super.onEntityCollision(state, worldIn, pos, entityIn);
	}
}