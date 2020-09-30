package io.github.apace100.origins.power.factory.condition.item;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Supplier;

public class SimpleItemConditionSerializer extends ItemCondition.Serializer {

    private final Supplier<ItemCondition> conditionSupplier;

    public SimpleItemConditionSerializer(Supplier<ItemCondition> conditionSupplier) {
        this.conditionSupplier = conditionSupplier;
    }

    @Override
    public void write(ItemCondition condition, PacketByteBuf buf) {

    }

    @Override
    public ItemCondition read(PacketByteBuf buf) {
        return conditionSupplier.get();
    }

    @Override
    public ItemCondition read(JsonObject json) {
        return conditionSupplier.get();
    }
}
