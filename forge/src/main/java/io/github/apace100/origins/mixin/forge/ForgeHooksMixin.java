package io.github.apace100.origins.mixin.forge;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ModifyHarvestPower;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeHooks.class)
public class ForgeHooksMixin {

	@Inject(method = "canHarvestBlock", remap = false, at = @At("HEAD"), cancellable = true)
	private static void canHarvestBlockHook(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		for (ModifyHarvestPower mhp : OriginComponent.getPowers(player, ModifyHarvestPower.class)) {
			if (mhp.doesApply(pos)) {
				cir.setReturnValue(ForgeEventFactory.doPlayerHarvestCheck(player, state, mhp.isHarvestAllowed()));
				cir.cancel();
			}
		}
	}
}
