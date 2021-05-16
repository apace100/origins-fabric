package io.github.apace100.origins.power;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.function.Predicate;

public class RestrictArmorPower extends Power {
    private final HashMap<EquipmentSlot, Predicate<ItemStack>> armorConditions;

    public RestrictArmorPower(PowerType<?> type, PlayerEntity player, HashMap<EquipmentSlot, Predicate<ItemStack>> armorConditions) {
        super(type, player);
        this.armorConditions = armorConditions;
    }

    @Override
    public void onChosen(boolean isOrbOfOrigin) {
        super.onChosen(isOrbOfOrigin);
        for(EquipmentSlot slot : armorConditions.keySet()) {
            ItemStack equippedItem = player.getEquippedStack(slot);
            if(!equippedItem.isEmpty()) {
                if(!canEquip(equippedItem, slot)) {
                    if(!player.inventory.insertStack(equippedItem)) {
                        player.dropItem(equippedItem, true);
                    }
                    player.equipStack(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    public boolean canEquip(ItemStack itemStack, EquipmentSlot slot) {
        if (armorConditions.get(slot) == null) return true;
        return !armorConditions.get(slot).test(itemStack);
    }
}
