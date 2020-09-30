package io.github.apace100.origins.power.factory.condition.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.apace100.origins.registry.ModRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class PlayerCondition /*extends Condition<PlayerEntity>*/ implements Predicate<PlayerEntity> {

    protected boolean isInverted;

    public final boolean test(PlayerEntity playerEntity) {
        boolean fulfilled = isFulfilled(playerEntity);
        if(isInverted) {
            return !fulfilled;
        } else {
            return fulfilled;
        }
    }

    protected abstract boolean isFulfilled(PlayerEntity playerEntity);

    public abstract Identifier getSerializerId();

    public static abstract class Serializer<T extends PlayerCondition> {

        public abstract void write(T condition, PacketByteBuf buf);

        @Environment(EnvType.CLIENT)
        public abstract T read(PacketByteBuf buf);

        public abstract T read(JsonObject json);
    }

    public static void write(PlayerCondition condition, PacketByteBuf buf) {
        Identifier serializerId = condition.getSerializerId();
        buf.writeString(serializerId.toString());
        PlayerCondition.Serializer serializer = ModRegistries.PLAYER_CONDITION_SERIALIZER.get(serializerId);
        serializer.write(condition, buf);
        buf.writeBoolean(condition.isInverted);
    }

    @Environment(EnvType.CLIENT)
    public static PlayerCondition read(PacketByteBuf buf) {
        Identifier type = Identifier.tryParse(buf.readString());
        PlayerCondition.Serializer serializer = ModRegistries.PLAYER_CONDITION_SERIALIZER.get(type);
        PlayerCondition condition = serializer.read(buf);
        condition.isInverted = buf.readBoolean();
        return condition;
    }

    public static PlayerCondition read(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            if(!obj.has("type")) {
                throw new JsonParseException("PlayerCondition json requires \"type\" identifier.");
            }
            String typeIdentifier = JsonHelper.getString(obj, "type");
            Identifier type = Identifier.tryParse(typeIdentifier);
            Optional<PlayerCondition.Serializer> optionalSerializer = ModRegistries.PLAYER_CONDITION_SERIALIZER.getOrEmpty(type);
            if(!optionalSerializer.isPresent()) {
                throw new JsonParseException("PlayerCondition json \"type\" is not defined.");
            }
            PlayerCondition.Serializer serializer = optionalSerializer.get();
            PlayerCondition condition = serializer.read(obj);
            condition.isInverted = JsonHelper.getBoolean(obj, "inverted", false);
            return condition;
        }
        throw new JsonParseException("PlayerCondition has to be a JsonObject!");
    }
}
