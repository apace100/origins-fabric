package io.github.apace100.origins.power.factory.condition.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.apace100.origins.registry.ModRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class ItemCondition implements Predicate<ItemStack> {

    protected boolean isInverted;

    public final boolean test(ItemStack stack) {
        boolean fulfilled = isFulfilled(stack);
        if(isInverted) {
            return !fulfilled;
        } else {
            return fulfilled;
        }
    }

    protected abstract boolean isFulfilled(ItemStack stack);

    public abstract Identifier getSerializerId();

    public static abstract class Serializer<T extends ItemCondition> {

        public abstract void write(T condition, PacketByteBuf buf);

        @Environment(EnvType.CLIENT)
        public abstract T read(PacketByteBuf buf);

        public abstract T read(JsonObject json);
    }

    public static void write(ItemCondition condition, PacketByteBuf buf) {
        Identifier serializerId = condition.getSerializerId();
        buf.writeString(serializerId.toString());
        Serializer serializer = ModRegistries.ITEM_CONDITION_SERIALIZER.get(serializerId);
        serializer.write(condition, buf);
        buf.writeBoolean(condition.isInverted);
    }

    @Environment(EnvType.CLIENT)
    public static ItemCondition read(PacketByteBuf buf) {
        Identifier type = Identifier.tryParse(buf.readString());
        Serializer serializer = ModRegistries.ITEM_CONDITION_SERIALIZER.get(type);
        ItemCondition condition = serializer.read(buf);
        condition.isInverted = buf.readBoolean();
        return condition;
    }

    public static ItemCondition read(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            if(!obj.has("type")) {
                throw new JsonParseException("ItemCondition json requires \"type\" identifier.");
            }
            String typeIdentifier = JsonHelper.getString(obj, "type");
            Identifier type = Identifier.tryParse(typeIdentifier);
            Optional<Serializer> optionalSerializer = ModRegistries.ITEM_CONDITION_SERIALIZER.getOrEmpty(type);
            if(!optionalSerializer.isPresent()) {
                throw new JsonParseException("ItemCondition json \"type\" is not defined.");
            }
            Serializer serializer = optionalSerializer.get();
            ItemCondition condition = serializer.read(obj);
            condition.isInverted = JsonHelper.getBoolean(obj, "inverted", false);
            return condition;
        }
        throw new JsonParseException("ItemCondition has to be a JsonObject!");
    }
}
