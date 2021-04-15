package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.OriginLootCondition;
import io.github.apace100.origins.util.PowerLootCondition;
import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.registry.Registry;

import static net.minecraft.util.registry.Registry.*;

public class ModLoot {
    public static final LootConditionType ORIGIN_LOOT_CONDITION = registerLootCondition("origin", new OriginLootCondition.Serializer());
    public static final LootConditionType POWER_LOOT_CONDITION = registerLootCondition("power", new PowerLootCondition.Serializer());

    /**
     * This is only really implemented on fabric because it is at best bad practise, and at worse actively sabotaging
     * vanilla loot table mechanics, declaring {@link Enchantment#isTreasure()} as true makes it viable for generation
     * in loot tables (and not in the enchantment table), which means that it already generates, thus this modification
     * is useless.
     */
    @ExpectPlatform
    public static void registerLootTables() {
        throw new AssertionError();
    }

    private static LootConditionType registerLootCondition(String path, JsonSerializer<? extends LootCondition> serializer) {
        return Registry.register(LOOT_CONDITION_TYPE, Origins.identifier(path), new LootConditionType(serializer));
    }
}
