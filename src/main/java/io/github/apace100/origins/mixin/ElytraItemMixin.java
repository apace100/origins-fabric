package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ElytraFlightPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElytraItem.class)
public class ElytraItemMixin {

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        if(user != null) {
            ItemStack stackInHand = user.getStackInHand(hand);
            if(OriginComponent.getPowers(user, ElytraFlightPower.class).size() > 0 && stackInHand.getItem() == Items.ELYTRA) {
                info.setReturnValue(TypedActionResult.fail(stackInHand));
            }
        }
    }
}
