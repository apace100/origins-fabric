package io.github.apace100.origins.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class ClientHooks {
	public static BlockState getInWallBlockState(PlayerEntity playerEntity) {
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for(int i = 0; i < 8; ++i) {
			double d = playerEntity.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
			double e = playerEntity.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
			double f = playerEntity.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
			mutable.set(d, e, f);
			BlockState blockState = playerEntity.world.getBlockState(mutable);
			if (blockState.getRenderType() != BlockRenderType.INVISIBLE && blockState.shouldBlockVision(playerEntity.world, mutable)) {
				return blockState;
			}
		}

		return null;
	}
}
