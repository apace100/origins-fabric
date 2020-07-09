package io.github.apace100.origins.mixin;

import io.github.apace100.origins.power.PowerTypes;
import io.github.apace100.origins.util.Constants;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/screen/PlayerScreenHandler$1")
public abstract class PlayerScreenHandlerMixin extends Slot {

    public PlayerScreenHandlerMixin(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Inject(method = "Lnet/minecraft/screen/PlayerScreenHandler$1;canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void preventArmorInsertion(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        PlayerEntity player = ((PlayerInventory)inventory).player;
        if(PowerTypes.LIGHT_ARMOR.isActive(player)) {
            if(stack.getItem() instanceof ArmorItem) {
                ArmorItem armor = ((ArmorItem)stack.getItem());
                EquipmentSlot slot = armor.getSlotType();

                if(armor.getProtection() > Constants.LIGHT_ARMOR_MAX_PROTECTION[slot.getEntitySlotId()]) {
                    info.setReturnValue(false);
                }
            }
        }
        if(PowerTypes.ELYTRA.isActive(player)) {
            if(stack.getItem() == Items.ELYTRA) {
                info.setReturnValue(false);
            }
        }
    }
}
