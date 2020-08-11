package io.github.apace100.origins.origin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.apace100.origins.Origins;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OriginLayer implements Comparable<OriginLayer> {

    private int order;
    private Identifier identifier;
    private List<Identifier> origins;
    private boolean enabled = false;

    private String nameTranslationKey;

    public String getOrCreateTranslationKey() {
        if(nameTranslationKey == null || nameTranslationKey.isEmpty()) {
            this.nameTranslationKey = "layer." + identifier.getNamespace() + "." + identifier.getPath();
        }
        return nameTranslationKey;
    }

    public String getTranslationKey() {
        return getOrCreateTranslationKey();
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<Identifier> getOrigins() {
        List<Identifier> filteredOrigins = origins.stream().filter(OriginRegistry::contains).collect(Collectors.toList());
        if(filteredOrigins.size() < origins.size()) {
            for (Identifier id : origins) {
                if(!OriginRegistry.contains(id)) {
                    Origins.LOGGER.error("Origin layer \"" + identifier.toString() + "\" contained unregistered origin: \"" + id.toString() + "\" (skipping)");
                }
            }
        }
        return filteredOrigins;
    }

    public boolean contains(Origin origin) {
        return origin == Origin.EMPTY || origins.contains(origin.getIdentifier());
    }

    public void merge(JsonObject json) {
        if(json.has("order")) {
            this.order = json.get("order").getAsInt();
        }
        if(json.has("enabled")) {
            this.enabled = json.get("enabled").getAsBoolean();
        }
        if(json.has("origins")) {
            JsonArray originArray = json.getAsJsonArray("origins");
            originArray.forEach(je -> {
                Identifier identifier = Identifier.tryParse(je.getAsString());
                this.origins.add(identifier);
                Origins.LOGGER.info("Added origin " + identifier.toString() + " to layer " + this.identifier.toString());
            });
        }
        if(json.has("name")) {
            this.nameTranslationKey = JsonHelper.getString(json, "name", "");
        }
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        } else if(!(obj instanceof OriginLayer)) {
            return false;
        } else {
            return identifier.equals(((OriginLayer)obj).identifier);
        }
    }

    @Override
    public int compareTo(OriginLayer o) {
        return Integer.compare(order, o.order);
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeString(identifier.toString());
        buffer.writeInt(order);
        buffer.writeBoolean(enabled);
        buffer.writeInt(origins.size());
        origins.forEach(id -> buffer.writeString(id.toString()));
        buffer.writeString(getOrCreateTranslationKey());
    }

    @Environment(EnvType.CLIENT)
    public static OriginLayer read(PacketByteBuf buffer) {
        OriginLayer layer = new OriginLayer();
        layer.identifier = Identifier.tryParse(buffer.readString());
        layer.order = buffer.readInt();
        layer.enabled = buffer.readBoolean();
        int originCount = buffer.readInt();
        layer.origins = new ArrayList<>(originCount);
        for(int i = 0; i < originCount; i++) {
            layer.origins.add(Identifier.tryParse(buffer.readString()));
        }
        layer.nameTranslationKey = buffer.readString();
        return layer;
    }

    public static OriginLayer fromJson(Identifier id, JsonObject json) {
        int order = JsonHelper.getInt(json, "order", OriginLayers.size());
        if(!json.has("origins") || !json.get("origins").isJsonArray()) {
            throw new JsonParseException("Origin layer JSON requires \"origins\" array of origin IDs to include in the layer.");
        }
        JsonArray originArray = json.getAsJsonArray("origins");
        List<Identifier> list = new ArrayList<>(originArray.size());
        originArray.forEach(je -> {
            Identifier identifier = Identifier.tryParse(je.getAsString());
            list.add(identifier);
        });
        boolean enabled = JsonHelper.getBoolean(json, "enabled", true);
        OriginLayer layer = new OriginLayer();
        layer.order = order;
        layer.origins = list;
        layer.enabled = enabled;
        layer.identifier = id;
        layer.nameTranslationKey = JsonHelper.getString(json, "name", "");
        return layer;
    }
}
