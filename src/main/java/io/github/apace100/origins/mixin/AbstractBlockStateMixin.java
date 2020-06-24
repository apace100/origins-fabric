package io.github.apace100.origins.mixin;

import io.github.apace100.origins.access.EntityShapeContextAccess;
import io.github.apace100.origins.power.PowerTypes;
import io.github.apace100.origins.registry.ModTags;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

    @Shadow
    public abstract Block getBlock();

    @Shadow protected abstract BlockState asBlockState();

    @Shadow public abstract VoxelShape getOutlineShape(BlockView world, BlockPos pos);

    @Inject(at = @At("HEAD"), method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", cancellable = true)
    private void phaseThroughBlocks(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> info) {
        VoxelShape blockShape = getBlock().getCollisionShape(asBlockState(), world, pos, context);
        if(!blockShape.isEmpty() && context instanceof EntityShapeContext && !getBlock().isIn(ModTags.UNPHASABLE)) {
            EntityShapeContext entityContext = (EntityShapeContext)context;
            if(!entityContext.isAbove(blockShape, pos, false) || entityContext.isDescending()) {
                Entity entity = ((EntityShapeContextAccess)context).getEntity();
                if(PowerTypes.PHASING.isActive(entity) && PowerTypes.PHASING.get(entity).isActive()) {
                    info.setReturnValue(VoxelShapes.empty());
                }

            }

        }
    }
}
