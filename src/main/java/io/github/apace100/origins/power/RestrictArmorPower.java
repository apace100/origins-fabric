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

    public boolean canEquip(ItemStack itemStack, EquipmentSlot slot) {
        return !armorConditions.get(slot).test(itemStack);
    }
}
