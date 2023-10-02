package io.github.apace100.origins.data;

import com.google.gson.JsonPrimitive;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class CompatibilityDataTypes {

    public static final SerializableDataType<ItemStack> ITEM_OR_ITEM_STACK = new SerializableDataType<>(
        ItemStack.class,
        SerializableDataTypes.ITEM_STACK::send,
        SerializableDataTypes.ITEM_STACK::receive,
        jsonElement -> {

            if (!(jsonElement instanceof JsonPrimitive jsonPrimitive) || !jsonPrimitive.isString()) {
                return SerializableDataTypes.ITEM_STACK.read(jsonElement);
            }

            Item item = SerializableDataTypes.ITEM.read(jsonPrimitive);
            return new ItemStack(item);

        },
        SerializableDataTypes.ITEM_STACK::write
    );

}
