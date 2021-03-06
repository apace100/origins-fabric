package io.github.apace100.origins.mixin;

import io.github.apace100.origins.access.EntityShapeContextAccess;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ModifyBreakSpeedPower;
import io.github.apace100.origins.power.PreventBlockSelectionPower;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {

    @Inject(at = @At("RETURN"), method = "calcBlockBreakingDelta", cancellable = true)
    private void modifyBlockBreakSpeed(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> info) {
        float base = info.getReturnValue();
        float modified = OriginComponent.modify(player, ModifyBreakSpeedPower.class, base, p -> p.doesApply(player.world, pos));
        info.setReturnValue(modified);
    }

    @Inject(at = @At("RETURN"), method = "getOutlineShape", cancellable = true)
    private void modifyBlockOutline(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        Entity entity = ((EntityShapeContextAccess)context).getEntity();
        if(entity != null) {
            if(OriginComponent.getPowers(entity, PreventBlockSelectionPower.class).stream().anyMatch(p -> p.doesPrevent(entity.world, pos))) {
                cir.setReturnValue(VoxelShapes.empty());
            }
        }
    }
}
