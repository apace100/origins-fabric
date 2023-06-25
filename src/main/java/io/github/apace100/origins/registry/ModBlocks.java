package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.content.TemporaryCobwebBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block TEMPORARY_COBWEB = new TemporaryCobwebBlock(FabricBlockSettings.create().mapColor(MapColor.WHITE_GRAY).solid().noCollision().requiresTool().strength(4.0F));

    public static void register() {
        register("temporary_cobweb", TEMPORARY_COBWEB, false);
    }

    private static void register(String blockName, Block block) {
        register(blockName, block, true);
    }

    private static void register(String blockName, Block block, boolean withBlockItem) {
        Registry.register(Registries.BLOCK, new Identifier(Origins.MODID, blockName), block);
        if(withBlockItem) {
            Registry.register(Registries.ITEM, new Identifier(Origins.MODID, blockName), new BlockItem(block, new Item.Settings()));
        }
    }
}