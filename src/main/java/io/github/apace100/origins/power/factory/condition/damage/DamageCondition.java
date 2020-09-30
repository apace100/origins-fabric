package io.github.apace100.origins.power.factory.condition.damage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.apace100.origins.registry.ModRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class DamageCondition implements Predicate<Pair<DamageSource, Float>> {

    protected boolean isInverted;

    public final boolean test(Pair<DamageSource, Float> damage) {
        boolean fulfilled = isFulfilled(damage);
        if(isInverted) {
            return !fulfilled;
        } else {
            return fulfilled;
        }
    }

    protected abstract boolean isFulfilled(Pair<DamageSource, Float> damage);

    public abstract Identifier getSerializerId();

    public static abstract class Serializer<T extends DamageCondition> {

        public abstract void write(T condition, PacketByteBuf buf);

        @Environment(EnvType.CLIENT)
        public abstract T read(PacketByteBuf buf);

        public abstract T read(JsonObject json);
    }

    public static void write(DamageCondition condition, PacketByteBuf buf) {
        Identifier serializerId = condition.getSerializerId();
        buf.writeString(serializerId.toString());
        Serializer serializer = ModRegistries.DAMAGE_CONDITION_SERIALIZER.get(serializerId);
        serializer.write(condition, buf);
        buf.writeBoolean(condition.isInverted);
    }

    @Environment(EnvType.CLIENT)
    public static DamageCondition read(PacketByteBuf buf) {
        Identifier type = Identifier.tryParse(buf.readString());
        Serializer serializer = ModRegistries.DAMAGE_CONDITION_SERIALIZER.get(type);
        DamageCondition condition = serializer.read(buf);
        condition.isInverted = buf.readBoolean();
        return condition;
    }

    public static DamageCondition read(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            if(!obj.has("type")) {
                throw new JsonParseException("DamageCondition json requires \"type\" identifier.");
            }
            String typeIdentifier = JsonHelper.getString(obj, "type");
            Identifier type = Identifier.tryParse(typeIdentifier);
            Optional<Serializer> optionalSerializer = ModRegistries.DAMAGE_CONDITION_SERIALIZER.getOrEmpty(type);
            if(!optionalSerializer.isPresent()) {
                throw new JsonParseException("DamageCondition json \"type\" is not defined.");
            }
            Serializer serializer = optionalSerializer.get();
            DamageCondition condition = serializer.read(obj);
            condition.isInverted = JsonHelper.getBoolean(obj, "inverted", false);
            return condition;
        }
        throw new JsonParseException("DamageCondition has to be a JsonObject!");
    }
}
