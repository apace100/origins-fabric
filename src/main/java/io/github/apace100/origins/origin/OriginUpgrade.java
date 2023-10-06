package io.github.apace100.origins.origin;

import com.google.gson.JsonElement;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.data.OriginsDataTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@Deprecated
public record OriginUpgrade(Identifier advancementCondition, Identifier upgradeToOrigin, @Nullable String announcement) {

    public static final SerializableData DATA = new SerializableData()
        .add("condition", SerializableDataTypes.IDENTIFIER)
        .add("origin", SerializableDataTypes.IDENTIFIER)
        .add("announcement", SerializableDataTypes.STRING, null);

    @Deprecated
    public Identifier getAdvancementCondition() {
        return advancementCondition;
    }

    @Deprecated
    public Identifier getUpgradeToOrigin() {
        return upgradeToOrigin;
    }

    @Deprecated
    @Nullable
    public String getAnnouncement() {
        return announcement;
    }

    public SerializableData.Instance toData() {

        SerializableData.Instance data = DATA.new Instance();

        data.set("condition", advancementCondition);
        data.set("origin", upgradeToOrigin);
        data.set("announcement", announcement);

        return data;

    }

    public static OriginUpgrade fromData(SerializableData.Instance data) {
        return new OriginUpgrade(data.get("condition"), data.get("origin"), data.get("announcement"));
    }

    public void write(PacketByteBuf buffer) {
        OriginsDataTypes.UPGRADE.send(buffer, this);
    }

    public static OriginUpgrade read(PacketByteBuf buffer) {
        return fromData(DATA.read(buffer));
    }

    public static OriginUpgrade fromJson(JsonElement jsonElement) {
        return OriginsDataTypes.UPGRADE.read(jsonElement);
    }

}
