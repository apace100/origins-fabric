package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ModifyHarvestPower;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow public ServerWorld world;
    @Shadow public ServerPlayerEntity player;
    private CachedBlockPosition cachedBlockPosition;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void cacheBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.cachedBlockPosition = new CachedBlockPosition(world, pos, true);
    }

    @ModifyVariable(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;postMine(Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;)V"), ordinal = 1)
    private boolean modifyEffectiveTool(boolean original) {
        for (ModifyHarvestPower mhp : OriginComponent.getPowers(player, ModifyHarvestPower.class)) {
            if (mhp.doesApply(cachedBlockPosition)) {
                return mhp.isHarvestAllowed();
            }
        }
        return original;
    }
}
