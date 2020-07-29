package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class ModTags {

    public static final Tag<Item> MEAT = TagRegistry.item(new Identifier(Origins.MODID, "meat"));
    public static final Tag<Block> UNPHASABLE = TagRegistry.block(new Identifier(Origins.MODID, "unphasable"));
    public static final Tag<Block> NATURAL_STONE = TagRegistry.block(new Identifier(Origins.MODID, "natural_stone"));
    public static final Tag<Item> RANGED_WEAPONS = TagRegistry.item(new Identifier(Origins.MODID, "ranged_weapons"));

    public static void register() {

    }
}
