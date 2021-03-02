package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.block.TemporaryCobwebBlock;
import me.shedaniel.architectury.registry.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block TEMPORARY_COBWEB = new TemporaryCobwebBlock(BlockProperties.of(Material.COBWEB).noCollision().requiresTool().strength(4.0F));

    public static void register() {
        register("temporary_cobweb", TEMPORARY_COBWEB, false);
    }

    private static void register(String blockName, Block block) {
        register(blockName, block, true);
    }

    private static void register(String blockName, Block block, boolean withBlockItem) {
        ModRegistries.BLOCKS.register(new Identifier(Origins.MODID, blockName), () -> block);
        if(withBlockItem) {
            ModRegistries.ITEMS.register(new Identifier(Origins.MODID, blockName), () -> new BlockItem(block, new Item.Settings().group(ItemGroup.MISC)));
        }
    }
}