package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import me.shedaniel.architectury.hooks.TagHooks;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class ModTags {

    public static final Tag<Item> MEAT = TagHooks.getItemOptional(new Identifier(Origins.MODID, "meat"));
    public static final Tag<Block> UNPHASABLE = TagHooks.getBlockOptional(new Identifier(Origins.MODID, "unphasable"));
    public static final Tag<Block> NATURAL_STONE = TagHooks.getBlockOptional(new Identifier(Origins.MODID, "natural_stone"));
    public static final Tag<Item> RANGED_WEAPONS = TagHooks.getItemOptional(new Identifier(Origins.MODID, "ranged_weapons"));

    public static void register() {

    }
}
