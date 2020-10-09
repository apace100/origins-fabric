package io.github.apace100.origins.power.factory.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class ConditionType<T> {

    private final Registry<Condition<T>> conditionRegistry;

    public ConditionType(Registry<Condition<T>> conditionRegistry) {
        this.conditionRegistry = conditionRegistry;
    }

    public void write(PacketByteBuf buf, Condition.Instance conditionInstance) {
        conditionInstance.write(buf);
    }

    @Environment(EnvType.CLIENT)
    public Condition<T>.Instance read(PacketByteBuf buf) {
        Identifier type = Identifier.tryParse(buf.readString());
        Condition<T> condition = conditionRegistry.get(type);
        Condition<T>.Instance conditionInstance = condition.read(buf);
        return conditionInstance;
    }

    public Condition<T>.Instance read(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            if(!obj.has("type")) {
                throw new JsonParseException("Condition json requires \"type\" identifier.");
            }
            String typeIdentifier = JsonHelper.getString(obj, "type");
            Identifier type = Identifier.tryParse(typeIdentifier);
            Optional<Condition<T>> optionalCondition = conditionRegistry.getOrEmpty(type);
            if(!optionalCondition.isPresent()) {
                throw new JsonParseException("Condition json \"type\" is not defined.");
            }
            return optionalCondition.get().read(obj);
        }
        throw new JsonParseException("Condition has to be a JsonObject!");
    }
}
