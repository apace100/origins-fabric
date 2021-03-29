package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ActionOnBlockBreakPower;
import io.github.apace100.origins.power.ModifyHarvestPower;
import io.github.apace100.origins.power.PreventBlockUsePower;
import io.github.apace100.origins.util.SavedBlockPosition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow public ServerWorld world;
    @Shadow public ServerPlayerEntity player;
    private SavedBlockPosition savedBlockPosition;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void cacheBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.savedBlockPosition = new SavedBlockPosition(world, pos);
    }

    @ModifyVariable(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;postMine(Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;)V"), ordinal = 1)
    private boolean modifyEffectiveTool(boolean original) {
        for (ModifyHarvestPower mhp : OriginComponent.getPowers(player, ModifyHarvestPower.class)) {
            if (mhp.doesApply(savedBlockPosition)) {
                return mhp.isHarvestAllowed();
            }
        }
        return original;
    }

    @Inject(method = "tryBreakBlock", at = @At(value = "RETURN", ordinal = 4), locals = LocalCapture.CAPTURE_FAILHARD)
    private void actionOnBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState blockState, BlockEntity blockEntity, Block block, boolean bl, ItemStack itemStack, ItemStack itemStack2, boolean bl2) {
        OriginComponent.getPowers(player, ActionOnBlockBreakPower.class).stream().filter(p -> p.doesApply(savedBlockPosition))
            .forEach(aobbp -> aobbp.executeActions(bl && bl2, pos, null));
    }


    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;shouldCancelInteraction()Z"), cancellable = true)
    private void preventBlockInteraction(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(OriginComponent.getPowers(player, PreventBlockUsePower.class).stream().anyMatch(p -> p.doesPrevent(world, hitResult.getBlockPos()))) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
