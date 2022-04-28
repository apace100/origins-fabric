package io.github.apace100.origins.screen.badge;

import com.google.gson.JsonObject;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.Origins;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

public class TooltipBadge extends Badge {
    private static final Identifier ID = Origins.identifier("tooltip");
    protected final String rawText;

    public TooltipBadge(Identifier spriteLocation, String rawText) {
        super(spriteLocation);
        this.rawText = rawText;
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    public List<Text> getTooltipText() {
        List<Text> texts = new LinkedList<>();
        for(String text : rawText.split("\n")) {
            texts.add(new TranslatableText(text));
        }
        return texts;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<TooltipComponent> getTooltipComponent(TextRenderer textRenderer, int widthLimit, float time) {
        List<TooltipComponent> tooltipComponents = new LinkedList<>();
        for(Text text : this.getTooltipText()) {
            if(textRenderer.getWidth(text) > widthLimit) {
                for(OrderedText orderedText : textRenderer.wrapLines(text, widthLimit)) {
                    tooltipComponents.add(new OrderedTextTooltipComponent(orderedText));
                }
            } else {
                tooltipComponents.add(new OrderedTextTooltipComponent(text.asOrderedText()));
            }
        }
        return tooltipComponents;
    }

    public static BadgeFactory<TooltipBadge> tooltipBadgeFactory() {
        SerializableData data = new SerializableData()
            .add("sprite", SerializableDataTypes.IDENTIFIER)
            .add("text", SerializableDataTypes.STRING, "");
        return new BadgeFactory<>(
            ID, data,
            (powerType, jsonObject) -> data.read(jsonObject),
            instance -> new TooltipBadge(
                instance.getId("sprite"),
                instance.getString("text")
            )
        );
    }

}
