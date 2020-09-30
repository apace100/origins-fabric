package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.RestrictArmorPower;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ArmorItem.class)
public class ArmorItemMixin {

    @Inject(method = "use", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void preventArmorEquipping(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info, ItemStack itemStack, EquipmentSlot equipmentSlot) {
        OriginComponent component = ModComponents.ORIGIN.get(user);
        if(component.getPowers(RestrictArmorPower.class).stream().anyMatch(rap -> !rap.canEquip(itemStack, equipmentSlot))) {
            info.setReturnValue(TypedActionResult.fail(itemStack));
        }
    }
}
