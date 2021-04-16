package io.github.apace100.origins.mixin.forge;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ActionOnBlockBreakPower;
import io.github.apace100.origins.util.SavedBlockPosition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Inject(method = "tryBreakBlock", at = @At(value = "RETURN", ordinal = 5), locals = LocalCapture.CAPTURE_FAILHARD)
    private void actionOnBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState blockState, int exp, BlockEntity blockEntity, Block block, ItemStack itemStack, ItemStack itemStack2, boolean harvest, boolean removed) {
        OriginComponent.getPowers(player, ActionOnBlockBreakPower.class).stream().filter(p -> p.doesApply(savedBlockPosition))
            .forEach(aobbp -> aobbp.executeActions(harvest && removed, pos, null));
    }
}
