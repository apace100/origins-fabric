package io.github.apace100.origins.origin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.data.OriginsDataTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class OriginUpgrade {

    public static final SerializableData DATA = new SerializableData()
        .add("condition", SerializableDataTypes.IDENTIFIER)
        .add("origin", SerializableDataTypes.IDENTIFIER)
        .add("announcement", SerializableDataTypes.STRING, null);

    private final Identifier advancementCondition;
    private final Identifier upgradeToOrigin;
    private final String announcement;

    public OriginUpgrade(Identifier advancementCondition, Identifier upgradeToOrigin, String announcement) {
        this.advancementCondition = advancementCondition;
        this.upgradeToOrigin = upgradeToOrigin;
        this.announcement = announcement;
    }

    public Identifier getAdvancementCondition() {
        return advancementCondition;
    }

    public Identifier getUpgradeToOrigin() {
        return upgradeToOrigin;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void write(PacketByteBuf buffer) {
        OriginsDataTypes.UPGRADE.send(buffer, this);
    }

    public static OriginUpgrade read(PacketByteBuf buffer) {
        SerializableData.Instance data = DATA.read(buffer);
        return new OriginUpgrade(data.get("condition"), data.get("origin"), data.get("announcement"));
    }

    public static OriginUpgrade fromJson(JsonElement jsonElement) {
        if(!jsonElement.isJsonObject()) {
            throw new JsonParseException("Origin upgrade needs to be a JSON object.");
        }
        JsonObject json = jsonElement.getAsJsonObject();
        JsonElement condition;
        JsonElement origin;
        if(json.has("condition") && (condition = json.get("condition")).isJsonPrimitive()
            && json.has("origin") && (origin = json.get("origin")).isJsonPrimitive()) {
            Identifier conditionId = Identifier.tryParse(condition.getAsString());
            Identifier originId = Identifier.tryParse(origin.getAsString());
            String announcement = "";
            if(json.has("announcement")) {
                JsonElement anno = json.get("announcement");
                if(anno.isJsonPrimitive()) {
                    announcement = anno.getAsString();
                }
            }
            return new OriginUpgrade(conditionId, originId, announcement);
        } else {
            throw new JsonParseException("Origin upgrade JSON requires \"condition\" string and \"origin\" string.");
        }
    }

}
