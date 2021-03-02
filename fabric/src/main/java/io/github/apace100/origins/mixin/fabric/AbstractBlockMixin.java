package io.github.apace100.origins.mixin.fabric;

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
        //Handled via event in forge
        float base = info.getReturnValue();
        float modified = OriginComponent.modify(player, ModifyBreakSpeedPower.class, base, p -> p.doesApply(player.world, pos));
        info.setReturnValue(modified);
    }
}
