package io.github.apace100.origins.registry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetEnchantmentsLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

public class ModLoot {

    private static final Set<RegistryKey<LootTable>> AFFECTED_TABLES = new HashSet<>();

    private static final RegistryKey<LootTable> SIMPLE_DUNGEON = getAndAddTable("chests/simple_dungeon");
    private static final RegistryKey<LootTable> STRONGHOLD_LIBRARY = getAndAddTable("chests/stronghold_library");
    private static final RegistryKey<LootTable> MINESHAFT = getAndAddTable("chests/abandoned_mineshaft");
    private static final RegistryKey<LootTable> SMALL_UNDERWATER_RUIN = getAndAddTable("chests/underwater_ruin_small");

    private static final BiFunction<RegistryEntry<Enchantment>, Integer, SetEnchantmentsLootFunction.Builder> SIMPLE_ENCHANTMENT_SETTER = (enchantmentEntry, levels) ->
        new SetEnchantmentsLootFunction.Builder()
            .enchantment(enchantmentEntry, ConstantLootNumberProvider.create(levels));

    private static RegistryKey<LootTable> getAndAddTable(String path) {

        RegistryKey<LootTable> tableKey = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(path));
        AFFECTED_TABLES.add(tableKey);

        return tableKey;

    }

    public static void registerLootTables() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            if (!AFFECTED_TABLES.contains(key)) {
                return;
            }

            RegistryEntry<Enchantment> waterProtection = registries
                .getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                .getOptional(ModEnchantments.WATER_PROTECTION).orElse(null);

            if (waterProtection == null) return;

            if (key.equals(SIMPLE_DUNGEON)) {
                tableBuilder.pool(new LootPool.Builder()
                    .rolls(ConstantLootNumberProvider.create(1.0F))
                    .with(ItemEntry.builder(Items.BOOK)
                        .weight(20)
                        .apply(SIMPLE_ENCHANTMENT_SETTER.apply(waterProtection, 1)))
                    .with(ItemEntry.builder(Items.BOOK)
                        .weight(10)
                        .apply(SIMPLE_ENCHANTMENT_SETTER.apply(waterProtection, 2)))
                    .with(EmptyEntry.builder()
                        .weight(80)));
            }

            else if (key.equals(STRONGHOLD_LIBRARY)) {
                tableBuilder.pool(new LootPool.Builder()
                    .rolls(ConstantLootNumberProvider.create(1.0F))
                    .with(ItemEntry.builder(Items.BOOK)
                        .weight(20)
                        .apply(SIMPLE_ENCHANTMENT_SETTER.apply(waterProtection, 2)))
                    .with(ItemEntry.builder(Items.BOOK)
                        .weight(10)
                        .apply(SIMPLE_ENCHANTMENT_SETTER.apply(waterProtection, 3)))
                    .with(EmptyEntry.builder()
                        .weight(80)));
            }

            else if (key.equals(MINESHAFT)) {
                tableBuilder.pool(new LootPool.Builder()
                    .rolls(ConstantLootNumberProvider.create(1.0F))
                    .with(ItemEntry.builder(Items.BOOK)
                        .weight(20)
                        .apply(SIMPLE_ENCHANTMENT_SETTER.apply(waterProtection, 1)))
                    .with(ItemEntry.builder(Items.BOOK)
                        .weight(5)
                        .apply(SIMPLE_ENCHANTMENT_SETTER.apply(waterProtection, 2)))
                    .with(EmptyEntry.builder()
                        .weight(90)));
            }

            else if (key.equals(SMALL_UNDERWATER_RUIN)) {
                tableBuilder.pool(new LootPool.Builder()
                    .rolls(ConstantLootNumberProvider.create(1.0F))
                    .with(ItemEntry.builder(Items.BOOK)
                        .weight(10)
                        .apply(SIMPLE_ENCHANTMENT_SETTER.apply(waterProtection, 1)))
                    .with(ItemEntry.builder(Items.BOOK)
                        .weight(20)
                        .apply(SIMPLE_ENCHANTMENT_SETTER.apply(waterProtection, 2)))
                    .with(EmptyEntry.builder()
                        .weight(110)));
            }

        });
    }

}
