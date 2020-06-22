package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class ModTags {

    public static Tag<Item> MEAT;
    public static Tag<Block> UNPHASABLE;

    public static void register() {
        MEAT = TagRegistry.item(new Identifier(Origins.MODID, "meat"));
        UNPHASABLE = TagRegistry.block(new Identifier(Origins.MODID, "unphasable"));
    }
}
