package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.enchantment.WaterProtectionEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public class ModEnchantments {

    public static final Enchantment WATER_PROTECTION = new WaterProtectionEnchantment(Enchantment.Rarity.RARE, EnchantmentTarget.ARMOR, new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});

    public static void register() {
        register("water_protection", WATER_PROTECTION);
    }

    private static Enchantment register(String path, Enchantment enchantment) {
        Registry.register(Registries.ENCHANTMENT, new Identifier(Origins.MODID, path), enchantment);
        return enchantment;
    }
}
