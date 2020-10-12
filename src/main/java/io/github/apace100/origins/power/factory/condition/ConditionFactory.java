package io.github.apace100.origins.power.factory.condition;

import com.google.gson.JsonObject;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ConditionFactory<T> {

    private final Identifier identifier;
    protected SerializableData data;
    private final BiFunction<SerializableData.Instance, T, Boolean> condition;

    public ConditionFactory(Identifier identifier, SerializableData data, BiFunction<SerializableData.Instance, T, Boolean> condition) {
        this.identifier = identifier;
        this.condition = condition;
        this.data = data;
        this.data.add("inverted", SerializableDataType.BOOLEAN, false);
    }

    public class Instance implements Predicate<T> {

        private final SerializableData.Instance dataInstance;

        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
        }

        public final boolean test(T t) {
            boolean fulfilled = isFulfilled(t);
            if(dataInstance.getBoolean("inverted")) {
                return !fulfilled;
            } else {
                return fulfilled;
            }
        }

        public boolean isFulfilled(T t) {
            return condition.apply(dataInstance, t);
        }

        public void write(PacketByteBuf buf) {
            buf.writeIdentifier(identifier);
            data.write(buf, dataInstance);
        }
    }

    public Identifier getSerializerId() {
        return identifier;
    }

    public Instance read(JsonObject json) {
        return new Instance(data.read(json));
    }

    public Instance read(PacketByteBuf buffer) {
        return new Instance(data.read(buffer));
    }
}
