package io.github.apace100.origins.badge;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

public record TooltipBadge(Identifier spriteId, Text text) implements Badge {

    public TooltipBadge(SerializableData.Instance instance) {
        this(instance.getId("sprite"), instance.get("text"));
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    public static void addLines(List<TooltipComponent> tooltips, Text text, TextRenderer textRenderer, int widthLimit) {
        if(textRenderer.getWidth(text) > widthLimit) {
            for(OrderedText orderedText : textRenderer.wrapLines(text, widthLimit)) {
                tooltips.add(new OrderedTextTooltipComponent(orderedText));
            }
        } else {
            tooltips.add(new OrderedTextTooltipComponent(text.asOrderedText()));
        }
    }

    @Override
    public List<TooltipComponent> getTooltipComponents(PowerType<?> powerType, int widthLimit, float time, TextRenderer textRenderer) {
        List<TooltipComponent> tooltips = new LinkedList<>();
        addLines(tooltips, text, textRenderer, widthLimit);
        return tooltips;
    }

    @Override
    public SerializableData.Instance toData(SerializableData.Instance instance) {
        instance.set("sprite", spriteId);
        instance.set("text", text);
        return instance;
    }

    @Override
    public BadgeFactory getBadgeFactory() {
        return BadgeFactories.TOOLTIP;
    }

}
