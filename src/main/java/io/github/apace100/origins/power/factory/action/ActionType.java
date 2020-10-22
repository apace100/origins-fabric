package io.github.apace100.origins.power.factory.action;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class ActionType<T> {

    private final String actionTypeName;
    private final Registry<ActionFactory<T>> actionFactoryRegistry;

    public ActionType(String actionTypeName, Registry<ActionFactory<T>> actionFactoryRegistry) {
        this.actionTypeName = actionTypeName;
        this.actionFactoryRegistry = actionFactoryRegistry;
    }

    public void write(PacketByteBuf buf, ActionFactory.Instance actionInstance) {
        actionInstance.write(buf);
    }

    public ActionFactory<T>.Instance read(PacketByteBuf buf) {
        Identifier type = buf.readIdentifier();
        ActionFactory<T> actionFactory = actionFactoryRegistry.get(type);
        if(actionFactory == null) {
            throw new JsonSyntaxException(actionTypeName + " \"" + type + "\" was not registered.");
        }
        return actionFactory.read(buf);
    }

    public ActionFactory<T>.Instance read(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            if(!obj.has("type")) {
                throw new JsonSyntaxException(actionTypeName + " json requires \"type\" identifier.");
            }
            String typeIdentifier = JsonHelper.getString(obj, "type");
            Identifier type = Identifier.tryParse(typeIdentifier);
            Optional<ActionFactory<T>> optionalAction = actionFactoryRegistry.getOrEmpty(type);
            if(!optionalAction.isPresent()) {
                throw new JsonSyntaxException(actionTypeName + " json type \"" + type.toString() + "\" is not defined.");
            }
            return optionalAction.get().read(obj);
        }
        throw new JsonSyntaxException(actionTypeName + " has to be a JsonObject!");
    }
}
