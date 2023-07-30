package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public class ModTags {

    public static final TagKey<Item> MEAT = TagKey.of(RegistryKeys.ITEM, new Identifier(Origins.MODID, "meat"));
    public static final TagKey<Block> UNPHASABLE = TagKey.of(RegistryKeys.BLOCK, new Identifier(Origins.MODID, "unphasable"));
    public static final TagKey<Block> NATURAL_STONE = TagKey.of(RegistryKeys.BLOCK, new Identifier(Origins.MODID, "natural_stone"));
    public static final TagKey<Item> RANGED_WEAPONS = TagKey.of(RegistryKeys.ITEM, new Identifier(Origins.MODID, "ranged_weapons"));

    public static void register() {

    }
}
