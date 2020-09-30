package io.github.apace100.origins.power.factory.condition;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public abstract class Condition<T> implements Predicate<T> {

    protected boolean isInverted;

    public final boolean test(T t) {
        boolean fulfilled = isFulfilled(t);
        if(isInverted) {
            return !fulfilled;
        } else {
            return fulfilled;
        }
    }

    protected abstract boolean isFulfilled(T t);

    public abstract Identifier getSerializerId();

    public static abstract class Serializer<T extends Condition<?>> {

        public abstract void write(T condition, PacketByteBuf buf);

        @Environment(EnvType.CLIENT)
        public abstract T read(PacketByteBuf buf);

        public abstract T read(JsonObject json);
    }


}
