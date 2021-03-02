package io.github.apace100.origins.registry;

import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.enchantment.Enchantment;

public class ModLoot {
    /**
     * This is only really implemented on fabric because it is at best bad practise, and at worse actively sabotaging
     * vanilla loot table mechanics, declaring {@link Enchantment#isTreasure()} as true makes it viable for generation
     * in loot tables (and not in the enchantment table), which means that it already generates, thus this modification
     * is useless.
     */
    @ExpectPlatform
    public static void register() {
        throw new AssertionError();
    }
}
