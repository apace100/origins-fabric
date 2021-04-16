package io.github.apace100.origins.mixin.forge;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ClimbingPower;
import io.github.apace100.origins.power.ModifyHarvestPower;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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

	@Inject(method = "isLivingOnLadder", remap = false, at = @At("RETURN"), cancellable = true)
	private static void ladder(BlockState state, World world, BlockPos pos, LivingEntity entity, CallbackInfoReturnable<Boolean> info) {
		if(!info.getReturnValue()) {
			if(entity instanceof PlayerEntity) {
				List<ClimbingPower> climbingPowers = ModComponents.getOriginComponent(entity).getPowers(ClimbingPower.class, true);
				if(climbingPowers.size() > 0) {
					if(climbingPowers.stream().anyMatch(ClimbingPower::isActive)) {
						info.setReturnValue(true);
					} else if(entity.isHoldingOntoLadder()) {
						//if(origins_lastClimbingPos != null && isHoldingOntoLadder()) {
						if(climbingPowers.stream().anyMatch(ClimbingPower::canHold)) {
							info.setReturnValue(true);
						}
						//}
					}
				}
			}
		}
	}
}
