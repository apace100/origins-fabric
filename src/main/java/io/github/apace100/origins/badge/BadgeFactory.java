package io.github.apace100.origins.badge;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableData.Instance;
import io.github.apace100.calio.registry.DataObjectFactory;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public record BadgeFactory(Identifier id, SerializableData data, Function<SerializableData.Instance, Badge> factory) implements DataObjectFactory<Badge> {

    @Override
    public SerializableData getData() {
        return data;
    }

    @Override
    public Badge fromData(SerializableData.Instance instance) {
        return factory.apply(instance);
    }

    @Override
    public SerializableData.Instance toData(Badge badge) {
        return badge.toData(data.new Instance());
    }

}