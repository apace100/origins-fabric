package io.github.apace100.origins.screen.badge;

import com.google.gson.JsonObject;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.Origins;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Badge {
    private static final Identifier ID = Origins.identifier("simple");
    private final Identifier spriteLocation;

    public Badge(Identifier spriteLocation) {
        this.spriteLocation = spriteLocation;
    }

    public Identifier getSpriteLocation() {
        return spriteLocation;
    }

    public boolean hasTooltip() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    public List<TooltipComponent> getTooltipComponent(TextRenderer textRenderer, int widthLimit, float time) {
        return new ArrayList<>();
    }

    public static BadgeFactory<Badge> simpleBadgeFactory() {
        SerializableData data = new SerializableData()
            .add("sprite", SerializableDataTypes.IDENTIFIER);
        return new BadgeFactory<>(
            ID, data,
            (powerType, jsonObject) -> data.read(jsonObject),
            instance -> new Badge(instance.getId("sprite"))
        );
    }

}
