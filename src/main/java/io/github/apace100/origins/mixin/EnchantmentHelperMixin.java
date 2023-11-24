package io.github.apace100.origins.mixin;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.enchantment.WaterProtectionEnchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(at=@At("RETURN"), method="getPossibleEntries")
    private static void getPossibleEntries(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> ci) {
        if ((!treasureAllowed && !Origins.config.waterProtection.enchantingTable) || (treasureAllowed && !Origins.config.waterProtection.treasureOther)) {
            ci.getReturnValue().removeIf(ele -> ele != null && ele.enchantment instanceof WaterProtectionEnchantment);
        }
    }

}