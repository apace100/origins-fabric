package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ActionOnItemUsePower;
import io.github.apace100.origins.power.PreventItemUsePower;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        if(user != null) {
            OriginComponent component = ModComponents.ORIGIN.get(user);
            ItemStack stackInHand = user.getStackInHand(hand);
            for(PreventItemUsePower piup : component.getPowers(PreventItemUsePower.class)) {
                if(piup.doesPrevent(stackInHand)) {
                    info.setReturnValue(TypedActionResult.fail(stackInHand));
                    break;
                }
            }
        }
    }

    @Unique
    private ItemStack usedItemStack;

    @Inject(method = "finishUsing", at = @At("HEAD"))
    public void cacheUsedItemStack(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        usedItemStack = ((ItemStack)(Object)this).copy();
    }

    @Inject(method = "finishUsing", at = @At("RETURN"))
    public void callActionOnUse(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if(user instanceof PlayerEntity) {
            ItemStack returnStack = cir.getReturnValue();
            OriginComponent component = ModComponents.ORIGIN.get(user);
            for(ActionOnItemUsePower p : component.getPowers(ActionOnItemUsePower.class)) {
                if(p.doesApply(usedItemStack)) {
                    p.executeActions(returnStack);
                }
            }
        }
    }
}
