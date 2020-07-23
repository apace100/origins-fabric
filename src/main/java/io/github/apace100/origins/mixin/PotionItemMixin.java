package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.PowerTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/stat/Stat;)V"), method = "finishUsing")
    private void merlingWaterDrinking(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        if(PowerTypes.WATER_BREATHING.isActive(user)) {
            user.setAir(Math.min(user.getMaxAir(), user.getAir() + 60));
        }
    }
}
