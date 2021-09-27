package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.content.OrbOfOriginItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {

    public static final Item ORB_OF_ORIGIN = new OrbOfOriginItem();

    public static void register() {
        Registry.register(Registry.ITEM, new Identifier(Origins.MODID, "orb_of_origin"), ORB_OF_ORIGIN);
    }
}
