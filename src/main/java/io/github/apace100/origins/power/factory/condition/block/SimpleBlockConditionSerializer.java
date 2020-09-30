package io.github.apace100.origins.power.factory.condition.block;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Supplier;

public class SimpleBlockConditionSerializer extends BlockCondition.Serializer {

    private final Supplier<BlockCondition> conditionSupplier;

    public SimpleBlockConditionSerializer(Supplier<BlockCondition> conditionSupplier) {
        this.conditionSupplier = conditionSupplier;
    }

    @Override
    public void write(BlockCondition condition, PacketByteBuf buf) {

    }

    @Override
    public BlockCondition read(PacketByteBuf buf) {
        return conditionSupplier.get();
    }

    @Override
    public BlockCondition read(JsonObject json) {
        return conditionSupplier.get();
    }
}
