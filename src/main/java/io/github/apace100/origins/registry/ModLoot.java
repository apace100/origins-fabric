package io.github.apace100.origins.registry;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

public class ModLoot {

    private static final Identifier DUNGEON_LOOT = new Identifier("minecraft", "chests/simple_dungeon");
    private static final Identifier STRONGHOLD_LIBRARY = new Identifier("minecraft", "chests/stronghold_library");
    private static final Identifier MINESHAFT = new Identifier("minecraft", "chests/abandoned_mineshaft");
    private static final Identifier WATER_RUIN = new Identifier("minecraft", "chests/underwater_ruin_small");

    public static void register() {
        CompoundTag waterProtectionLevel1 = createEnchantmentTag(ModEnchantments.WATER_PROTECTION, 1);
        CompoundTag waterProtectionLevel2 = createEnchantmentTag(ModEnchantments.WATER_PROTECTION, 2);
        CompoundTag waterProtectionLevel3 = createEnchantmentTag(ModEnchantments.WATER_PROTECTION, 3);
        LootTableLoadingCallback.EVENT.register(((resourceManager, lootManager, identifier, fabricLootSupplierBuilder, lootTableSetter) -> {
            if(DUNGEON_LOOT.equals(identifier)) {
                FabricLootPoolBuilder lootPool = FabricLootPoolBuilder.builder()
                    .rolls(ConstantLootTableRange.create(1))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(20)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel1)))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(10)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel2)))
                    .with(EmptyEntry.Serializer().weight(80));
                fabricLootSupplierBuilder.withPool(lootPool.build());
            } else if(STRONGHOLD_LIBRARY.equals(identifier)) {
                FabricLootPoolBuilder lootPool = FabricLootPoolBuilder.builder()
                    .rolls(ConstantLootTableRange.create(1))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(20)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel2)))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(10)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel3)))
                    .with(EmptyEntry.Serializer().weight(80));
                fabricLootSupplierBuilder.withPool(lootPool.build());
            } else if(MINESHAFT.equals(identifier)) {
                FabricLootPoolBuilder lootPool = FabricLootPoolBuilder.builder()
                    .rolls(ConstantLootTableRange.create(1))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(20)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel1)))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(5)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel2)))
                    .with(EmptyEntry.Serializer().weight(90));
                fabricLootSupplierBuilder.withPool(lootPool.build());
            } else if(WATER_RUIN.equals(identifier)) {
                FabricLootPoolBuilder lootPool = FabricLootPoolBuilder.builder()
                    .rolls(ConstantLootTableRange.create(1))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(10)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel1)))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(20)
                        .apply(SetNbtLootFunction.builder(waterProtectionLevel2)))
                    .with(EmptyEntry.Serializer().weight(110));
                fabricLootSupplierBuilder.withPool(lootPool.build());
            }
        }));
    }

    private static CompoundTag createEnchantmentTag(Enchantment enchantment, int level) {
        EnchantmentLevelEntry entry = new EnchantmentLevelEntry(enchantment, level);
        return EnchantedBookItem.forEnchantment(entry).getTag();
    }
}
