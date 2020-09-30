package io.github.apace100.origins.power.factory.condition.item;

import io.github.apace100.origins.Origins;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class IsFoodCondition extends ItemCondition {

    public static final Identifier SERIALIZER = new Identifier(Origins.MODID, "is_food");

    @Override
    public boolean isFulfilled(ItemStack stack) {
        return stack.isFood();
    }

    @Override
    public Identifier getSerializerId() {
        return SERIALIZER;
    }
}
