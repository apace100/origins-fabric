package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModTags {

    public static final TagKey<Item> MEAT = TagKey.of(Registry.ITEM_KEY, new Identifier(Origins.MODID, "meat"));
    public static final TagKey<Block> UNPHASABLE = TagKey.of(Registry.BLOCK_KEY, new Identifier(Origins.MODID, "unphasable"));
    public static final TagKey<Block> NATURAL_STONE = TagKey.of(Registry.BLOCK_KEY, new Identifier(Origins.MODID, "natural_stone"));
    public static final TagKey<Item> RANGED_WEAPONS = TagKey.of(Registry.ITEM_KEY, new Identifier(Origins.MODID, "ranged_weapons"));

    public static void register() {

    }
}
