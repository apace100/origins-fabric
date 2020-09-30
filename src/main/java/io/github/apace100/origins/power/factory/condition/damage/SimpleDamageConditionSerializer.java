package io.github.apace100.origins.power.factory.condition.damage;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Supplier;

public class SimpleDamageConditionSerializer extends DamageCondition.Serializer {

    private final Supplier<DamageCondition> conditionSupplier;

    public SimpleDamageConditionSerializer(Supplier<DamageCondition> conditionSupplier) {
        this.conditionSupplier = conditionSupplier;
    }

    @Override
    public void write(DamageCondition condition, PacketByteBuf buf) {

    }

    @Override
    public DamageCondition read(PacketByteBuf buf) {
        return conditionSupplier.get();
    }

    @Override
    public DamageCondition read(JsonObject json) {
        return conditionSupplier.get();
    }
}
