package io.github.apace100.origins.power.factory.action;

import com.google.gson.JsonObject;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ActionFactory<T> {

    private final Identifier identifier;
    protected SerializableData data;
    private final BiConsumer<SerializableData.Instance, T> effect;

    public ActionFactory(Identifier identifier, SerializableData data, BiConsumer<SerializableData.Instance, T> effect) {
        this.identifier = identifier;
        this.effect = effect;
        this.data = data;
        this.data.add("inverted", SerializableDataType.BOOLEAN, false);
    }

    public class Instance implements Consumer<T> {

        private final SerializableData.Instance dataInstance;

        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
        }

        public void write(PacketByteBuf buf) {
            buf.writeIdentifier(identifier);
            data.write(buf, dataInstance);
        }

        @Override
        public void accept(T t) {
            effect.accept(dataInstance, t);
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
