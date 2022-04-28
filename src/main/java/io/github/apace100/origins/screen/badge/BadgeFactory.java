package io.github.apace100.origins.screen.badge;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BadgeFactory<B extends Badge> {
    private final Identifier id;
    protected SerializableData data;
    private final BiFunction<PowerType<?>, JsonObject, SerializableData.Instance> reader;
    private final Function<SerializableData.Instance, B> factory;

    public BadgeFactory(Identifier id, SerializableData data,
                        BiFunction<PowerType<?>, JsonObject, SerializableData.Instance> reader,
                        Function<SerializableData.Instance, B> factory) {
        this.id = id;
        this.data = data;
        this.reader = reader;
        this.factory = factory;
    }

    public class Instance implements Supplier<B> {

        private final SerializableData.Instance instance;

        private Instance(SerializableData.Instance instance) {
            this.instance = instance;
        }

        public BadgeFactory<B> getFactory() {
            return BadgeFactory.this;
        }

        @Override
        public B get() {
            return factory.apply(instance);
        }

        public void write(PacketByteBuf buf) {
            buf.writeIdentifier(this.getFactory().getSerializerId());
            data.write(buf, instance);
        }

    }

    public Identifier getSerializerId() {
        return id;
    }

    public BadgeFactory<B>.Instance read(PowerType<?> powerType, JsonObject jsonObject) {
        return new Instance(reader.apply(powerType, jsonObject));
    }

    public BadgeFactory<B>.Instance read(JsonObject jsonObject) {
        return new Instance(data.read(jsonObject));
    }

    public BadgeFactory<B>.Instance read(PacketByteBuf buffer) {
        return new Instance(data.read(buffer));
    }

}
