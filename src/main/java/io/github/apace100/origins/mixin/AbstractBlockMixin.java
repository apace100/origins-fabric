package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ModifyBreakSpeedPower;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
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
        float modified = base;
        for (ModifyBreakSpeedPower power : OriginComponent.getPowers(player, ModifyBreakSpeedPower.class)) {
            if(power.doesApply(player.world, pos)) {
                modified = power.apply(base, modified);
            }
        }
        info.setReturnValue(modified);/*
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
            if(PowerTypes.STRONG_ARMS.isActive(player) && !player.inventory.getMainHandStack().isEffectiveOn(state)) {
                info.setReturnValue(0.09F);
            }
        }*/
    }
}
