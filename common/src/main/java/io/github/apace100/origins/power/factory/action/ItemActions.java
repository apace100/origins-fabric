package io.github.apace100.origins.power.factory.action;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

public class ItemActions {

    public static void register() {
        register(new ActionFactory<>(Origins.identifier("consume"), new SerializableData()
            .add("amount", SerializableDataType.INT),
            (data, stack) -> stack.decrement(data.getInt("amount"))));
    }

    private static void register(ActionFactory<ItemStack> actionFactory) {
        ModRegistries.ITEM_ACTION.register(actionFactory.getSerializerId(), () -> actionFactory);
    }
}
