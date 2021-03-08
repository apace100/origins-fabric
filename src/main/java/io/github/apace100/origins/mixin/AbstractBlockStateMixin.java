package io.github.apace100.origins.mixin;

import io.github.apace100.origins.access.EntityShapeContextAccess;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.PhasingPower;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("deprecation")
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

    @Shadow
    public abstract Block getBlock();

    @Shadow protected abstract BlockState asBlockState();

    @Shadow public abstract VoxelShape getOutlineShape(BlockView world, BlockPos pos);

    @Inject(at = @At("HEAD"), method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", cancellable = true)
    private void phaseThroughBlocks(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> info) {
        VoxelShape blockShape = getBlock().getCollisionShape(asBlockState(), world, pos, context);
        if(!blockShape.isEmpty() && context instanceof EntityShapeContext) {
            Entity entity = ((EntityShapeContextAccess)context).getEntity();
            if(entity != null) {
                boolean isAbove = isAbove(entity, blockShape, pos, false);
                for (PhasingPower phasingPower : OriginComponent.getPowers(entity, PhasingPower.class)) {
                    if(!isAbove || phasingPower.shouldPhaseDown((PlayerEntity)entity)) {
                        if(phasingPower.doesApply(pos)) {
                            info.setReturnValue(VoxelShapes.empty());
                        }
                    }
                }
            }
        }
    }

    @Unique
    private boolean isAbove(Entity entity, VoxelShape shape, BlockPos pos, boolean defaultValue) {
        return entity.getY() > (double)pos.getY() + shape.getMax(Direction.Axis.Y) - (entity.isOnGround() ? 8.05/16.0 : 0.0015);
    }

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    private void preventCollisionWhenPhasing(World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        for (PhasingPower phasingPower : OriginComponent.getPowers(entity, PhasingPower.class)) {
            if(phasingPower.doesApply(pos)) {
                ci.cancel();
            }
        }
    }
}
