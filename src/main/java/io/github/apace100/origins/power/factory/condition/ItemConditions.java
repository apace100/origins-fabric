package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.Comparison;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class ItemConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Origins.identifier("constant"), new SerializableData()
            .add("value", SerializableDataType.BOOLEAN),
            (data, stack) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Origins.identifier("and"), new SerializableData()
            .add("conditions", SerializableDataType.ITEM_CONDITIONS),
            (data, stack) -> ((List<ConditionFactory<ItemStack>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(stack)
            )));
        register(new ConditionFactory<>(Origins.identifier("or"), new SerializableData()
            .add("conditions", SerializableDataType.ITEM_CONDITIONS),
            (data, stack) -> ((List<ConditionFactory<ItemStack>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(stack)
            )));
        register(new ConditionFactory<>(Origins.identifier("food"), new SerializableData(),
            (data, stack) -> stack.isFood()));
        register(new ConditionFactory<>(Origins.identifier("ingredient"), new SerializableData()
            .add("ingredient", SerializableDataType.INGREDIENT),
            (data, stack) -> ((Ingredient)data.get("ingredient")).test(stack)));
        register(new ConditionFactory<>(Origins.identifier("armor_value"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT),
            (data, stack) -> {
                int armor = 0;
                if(stack.getItem() instanceof ArmorItem) {
                    ArmorItem item = (ArmorItem)stack.getItem();
                    armor = item.getProtection();
                }
                return ((Comparison)data.get("comparison")).compare(armor, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Origins.identifier("harvest_level"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT),
            (data, stack) -> {
                int harvestLevel = 0;
                if(stack.getItem() instanceof ToolItem) {
                    ToolItem item = (ToolItem)stack.getItem();
                    harvestLevel = item.getMaterial().getMiningLevel();
                }
                return ((Comparison)data.get("comparison")).compare(harvestLevel, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Origins.identifier("enchantment"), new SerializableData()
            .add("enchantment", SerializableDataType.ENCHANTMENT)
            .add("compare_to", SerializableDataType.INT)
            .add("comparison", SerializableDataType.COMPARISON),
            (data, stack) -> {
                int enchantLevel = EnchantmentHelper.getLevel((Enchantment)data.get("enchantment"), stack);
                return ((Comparison)data.get("comparison")).compare(enchantLevel, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Origins.identifier("meat"), new SerializableData(),
            (data, stack) -> stack.isFood() && stack.getItem().getFoodComponent().isMeat()));
    }

    private static void register(ConditionFactory<ItemStack> conditionFactory) {
        Registry.register(ModRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
