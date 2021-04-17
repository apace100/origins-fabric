package io.github.apace100.origins.power.factory.action;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistriesArchitectury;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.item.ItemStack;

public class ItemActions {

    public static void register() {
        register(new ActionFactory<>(Origins.identifier("consume"), new SerializableData()
            .add("amount", SerializableDataType.INT, 1),
            (data, stack) -> stack.decrement(data.getInt("amount"))));
    }

    private static void register(ActionFactory<ItemStack> actionFactory) {
        ModRegistriesArchitectury.ITEM_ACTION.register(actionFactory.getSerializerId(), () -> actionFactory);
    }
}
