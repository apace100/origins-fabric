package io.github.apace100.origins.power.factory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.factory.condition.player.PlayerCondition;
import io.github.apace100.origins.registry.ModRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class PowerFactory<P extends Power> implements BiFunction<PowerType<P>, PlayerEntity, P> {

    protected List<List<PlayerCondition>> conditions = new LinkedList<>();

    @Override
    public abstract P apply(PowerType<P> powerType, PlayerEntity playerEntity);

    protected void addConditions(P power) {
        power.addCondition(player ->
            conditions.size() == 0 || conditions.stream().allMatch(ors -> ors.size() == 0 || ors.stream().anyMatch(condition -> condition.test(player)))
        );
    }

    public abstract Identifier getSerializerId();

    public static abstract class Serializer<T extends PowerFactory<?>> {

        public abstract void write(T factory, PacketByteBuf buf);
        @Environment(EnvType.CLIENT)
        public abstract T read(PacketByteBuf buf);

        public abstract T read(JsonObject json);

        protected void writeConditions(T factory, PacketByteBuf buf) {
            buf.writeInt(factory.conditions.size());
            for(List<PlayerCondition> conditionListInner : factory.conditions) {
                buf.writeInt(conditionListInner.size());
                for(PlayerCondition condition : conditionListInner) {
                    PlayerCondition.write(condition, buf);
                }
            }
        }

        @Environment(EnvType.CLIENT)
        protected void readConditions(T factory, PacketByteBuf buf) {
            factory.conditions.clear();
            int innerListCount = buf.readInt();
            for(int i = 0; i < innerListCount; i++) {
                int conditionCount = buf.readInt();
                List<PlayerCondition> innerList = new ArrayList<>(conditionCount);
                for(int j = 0; j < conditionCount; j++) {
                    innerList.add(PlayerCondition.read(buf));
                }
                factory.conditions.add(innerList);
            }
        }

        protected void readConditions(T factory, JsonObject json) {
            if(json.has("condition")) {
                JsonElement condElem = json.get("condition");
                if(condElem.isJsonObject()) {
                    PlayerCondition cond = PlayerCondition.read(condElem);
                    factory.conditions = new LinkedList<>();
                    LinkedList<PlayerCondition> innerList = new LinkedList<>();
                    innerList.add(cond);
                    factory.conditions.add(innerList);
                } else if(condElem.isJsonArray()) {
                    JsonArray condAndArray = condElem.getAsJsonArray();
                    factory.conditions = new LinkedList<>();
                    condAndArray.forEach(e0 -> {
                        LinkedList<PlayerCondition> orList = new LinkedList<>();
                        if(e0.isJsonArray()) {
                            JsonArray condOrArray = e0.getAsJsonArray();
                            condOrArray.forEach(e1 -> {
                                if(e1.isJsonObject()) {
                                    orList.add(PlayerCondition.read(e1));
                                }
                            });
                        }
                        if(orList.size() > 0) {
                            factory.conditions.add(orList);
                        }
                    });
                }
            }
        }
    }

    public static void write(PowerFactory factory, PacketByteBuf buf) {
        Identifier serializerId = factory.getSerializerId();
        buf.writeString(serializerId.toString());
        PowerFactory.Serializer serializer = ModRegistries.POWER_FACTORY_SERIALIZER.get(serializerId);
        serializer.write(factory, buf);
    }

    @Environment(EnvType.CLIENT)
    public static PowerFactory read(PacketByteBuf buf) {
        Identifier type = Identifier.tryParse(buf.readString());
        PowerFactory.Serializer serializer = ModRegistries.POWER_FACTORY_SERIALIZER.get(type);
        PowerFactory factory = serializer.read(buf);
        return factory;
    }

    public static PowerFactory read(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            if(!obj.has("type")) {
                throw new JsonParseException("PowerFactory json requires \"type\" identifier.");
            }
            String typeIdentifier = JsonHelper.getString(obj, "type");
            Identifier type = Identifier.tryParse(typeIdentifier);
            Optional<Serializer> optionalSerializer = ModRegistries.POWER_FACTORY_SERIALIZER.getOrEmpty(type);
            if(!optionalSerializer.isPresent()) {
                throw new JsonParseException("PowerFactory json \"type\" is not defined.");
            }
            PowerFactory.Serializer serializer = optionalSerializer.get();
            PowerFactory<?> factory = serializer.read(obj);
            return factory;
        }
        throw new JsonParseException("PowerFactory has to be a JsonObject!");
    }
}
