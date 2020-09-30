package io.github.apace100.origins.power.factory.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ConditionType<T, C extends Condition<T>, S extends Condition.Serializer<C>> {

    private final Registry<S> serializerRegistry;

    public ConditionType(Registry<S> serializerRegistry) {
        this.serializerRegistry = serializerRegistry;
    }


    public void write(C condition, PacketByteBuf buf) {
        Identifier serializerId = condition.getSerializerId();
        buf.writeString(serializerId.toString());
        S serializer = serializerRegistry.get(serializerId);
        serializer.write(condition, buf);
        buf.writeBoolean(condition.isInverted);
    }

    @Environment(EnvType.CLIENT)
    public C read(PacketByteBuf buf) {
        Identifier type = Identifier.tryParse(buf.readString());
        S serializer = serializerRegistry.get(type);
        C condition = serializer.read(buf);
        condition.isInverted = buf.readBoolean();
        return condition;
    }

    public C read(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            if(!obj.has("type")) {
                throw new JsonParseException("Condition json requires \"type\" identifier.");
            }
            String typeIdentifier = JsonHelper.getString(obj, "type");
            Identifier type = Identifier.tryParse(typeIdentifier);
            Optional<S> optionalSerializer = serializerRegistry.getOrEmpty(type);
            if(!optionalSerializer.isPresent()) {
                throw new JsonParseException("Condition json \"type\" is not defined.");
            }
            S serializer = optionalSerializer.get();
            C condition = serializer.read(obj);
            condition.isInverted = JsonHelper.getBoolean(obj, "inverted", false);
            return condition;
        }
        throw new JsonParseException("Condition has to be a JsonObject!");
    }

    public List<List<C>> readConditions(JsonElement element) {
        List<List<C>> list = new LinkedList<>();
        if(element.isJsonObject()) {
            C cond = read(element);
            LinkedList<C> innerList = new LinkedList<>();
            innerList.add(cond);
            list.add(innerList);
        } else if(element.isJsonArray()) {
            JsonArray condAndArray = element.getAsJsonArray();
            condAndArray.forEach(e0 -> {
                LinkedList<C> orList = new LinkedList<>();
                if(e0.isJsonArray()) {
                    JsonArray condOrArray = e0.getAsJsonArray();
                    condOrArray.forEach(e1 -> {
                        if(e1.isJsonObject()) {
                            orList.add(read(e1));
                        }
                    });
                }
                if(orList.size() > 0) {
                    list.add(orList);
                }
            });
        }
        return list;
    }

    public void writeConditions(List<List<C>> conditions, PacketByteBuf buf) {
        buf.writeInt(conditions.size());
        for(List<C> conditionListInner : conditions) {
            buf.writeInt(conditionListInner.size());
            for(C condition : conditionListInner) {
                write(condition, buf);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public List<List<C>> readPlayerConditions(PacketByteBuf buf) {
        List<List<C>> conditions = new LinkedList<>();
        int innerListCount = buf.readInt();
        for(int i = 0; i < innerListCount; i++) {
            int conditionCount = buf.readInt();
            List<C> innerList = new ArrayList<>(conditionCount);
            for(int j = 0; j < conditionCount; j++) {
                innerList.add(read(buf));
            }
            conditions.add(innerList);
        }
        return conditions;
    }

    public Predicate<T> buildConditionPredicate(List<List<C>> conditions) {
        return t ->
            conditions.size() == 0
                || conditions.stream().allMatch(ors -> ors.size() == 0 || ors.stream().anyMatch(condition -> condition.test(t)));
    }
}
