package io.github.apace100.origins.screen;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.Origins;
import net.minecraft.util.Identifier;

public class Badge {

    public static final SerializableData DATA = new SerializableData()
        .add("sprite", SerializableDataTypes.IDENTIFIER)
        .add("text", SerializableDataTypes.STRING);

    public static final Badge ACTIVE = new Badge(
        Origins.identifier("textures/gui/badge/active.png"),
        "origins.gui.badge.active");

    public static final Badge TOGGLE = new Badge(
        Origins.identifier("textures/gui/badge/toggle.png"),
        "origins.gui.badge.toggle");

    private final Identifier spriteLocation;
    private final String hoverText;

    public Badge(Identifier spriteLocation, String hoverText) {
        this.spriteLocation = spriteLocation;
        this.hoverText = hoverText;
    }

    public String getHoverText() {
        return hoverText;
    }

    public Identifier getSpriteLocation() {
        return spriteLocation;
    }

    public SerializableData.Instance getData() {
        SerializableData.Instance data = DATA.new Instance();
        data.set("sprite", spriteLocation);
        data.set("text", hoverText);
        return data;
    }

    public static Badge fromData(SerializableData.Instance data) {
        return new Badge(data.getId("sprite"), data.getString("text"));
    }
}
