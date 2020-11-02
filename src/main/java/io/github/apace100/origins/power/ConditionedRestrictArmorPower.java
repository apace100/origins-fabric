package io.github.apace100.origins.power;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.function.Predicate;

public class ConditionedRestrictArmorPower extends Power {

    private final HashMap<EquipmentSlot, Predicate<ItemStack>> armorConditions;
    private final int tickRate;

    public ConditionedRestrictArmorPower(PowerType<?> type, PlayerEntity player, HashMap<EquipmentSlot, Predicate<ItemStack>> armorConditions, int tickRate) {
        super(type, player);
        this.armorConditions = armorConditions;
        this.setTicking(true);
        this.tickRate = tickRate;
    }

    public boolean canEquip(ItemStack itemStack, EquipmentSlot slot) {
        return !armorConditions.get(slot).test(itemStack);
    }

    @Override
    public void tick() {
        if(player.age % tickRate == 0 && this.isActive()) {
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
    }
}
