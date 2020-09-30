package io.github.apace100.origins.power.factory.condition.player;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Supplier;

public class SimplePlayerConditionSerializer extends PlayerCondition.Serializer {

    private final Supplier<PlayerCondition> conditionSupplier;

    public SimplePlayerConditionSerializer(Supplier<PlayerCondition> conditionSupplier) {
        this.conditionSupplier = conditionSupplier;
    }

    @Override
    public void write(PlayerCondition condition, PacketByteBuf buf) {

    }

    @Override
    public PlayerCondition read(PacketByteBuf buf) {
        return conditionSupplier.get();
    }

    @Override
    public PlayerCondition read(JsonObject json) {
        return conditionSupplier.get();
    }
}
