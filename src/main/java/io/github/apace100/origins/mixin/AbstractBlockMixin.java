package io.github.apace100.origins.mixin;

import io.github.apace100.origins.access.EntityShapeContextAccess;
import io.github.apace100.origins.power.PowerTypes;
import io.github.apace100.origins.registry.ModTags;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {

    @Shadow
    protected boolean collidable;

    @Inject(at = @At("HEAD"), method = "getCollisionShape", cancellable = true)
    private void phaseThroughBlocks(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> info) {
        if(collidable && context instanceof EntityShapeContext && !state.getBlock().isIn(ModTags.UNPHASABLE)) {
            VoxelShape shape = state.getOutlineShape(world, pos);
            EntityShapeContext entityContext = (EntityShapeContext)context;
            if(!entityContext.isAbove(shape, pos, false) || entityContext.isDescending()) {
                Entity entity = ((EntityShapeContextAccess)context).getEntity();
                if(PowerTypes.PHASING.isActive(entity) && PowerTypes.PHASING.get(entity).isActive()) {
                    //if(entity.getBoundingBox().maxY > (double)pos.getY() + shape.getMin(Direction.Axis.Y)) {
                        info.setReturnValue(VoxelShapes.empty());
                    //}
                }

            }

        }
    }

    @Inject(at = @At("HEAD"), method = "calcBlockBreakingDelta", cancellable = true)
    private void modifyBlockBreakSpeed(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> info) {
        if(state.getBlock().isIn(ModTags.NATURAL_STONE)) {
            int adjacent = 0;
            for(Direction d : Direction.values()) {
                if(world.getBlockState(pos.offset(d)).getBlock().isIn(ModTags.NATURAL_STONE)) {
                    adjacent++;
                }
            }
            if(adjacent > 2) {
                if(PowerTypes.WEAK_ARMS.isActive(player) && !player.hasStatusEffect(StatusEffects.STRENGTH)) {
                    info.setReturnValue(0F);
                }
            }

        }
    }
}
