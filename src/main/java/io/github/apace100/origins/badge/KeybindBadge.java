package io.github.apace100.origins.badge;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.keybinding.KeyBindingUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.util.PowerKeyManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public record KeybindBadge(Identifier spriteId, String text) implements Badge {

    public KeybindBadge(SerializableData.Instance instance) {
        this(instance.getId("sprite"), instance.get("text"));
    }

    @Override
    public boolean hasTooltip() {
        return !text.isEmpty();
    }

    @Override
    public List<TooltipComponent> getTooltipComponents(Power power, int widthLimit, float time, TextRenderer textRenderer) {

        Optional<String> keyId = PowerKeyManager.getKeyId(power);
        List<TooltipComponent> tooltips = new ObjectArrayList<>();

        if (keyId.isPresent()) {

            Text keyName = KeyBindingUtil.getLocalizedName(keyId.get());
            Text keyText = Text.translatable(text(), Text.literal("[").append(keyName).append("]"));

            if (textRenderer.getWidth(keyText) > widthLimit) {
                textRenderer.wrapLines(keyText, widthLimit)
                    .stream()
                    .map(OrderedTextTooltipComponent::new)
                    .forEach(tooltips::add);
            }

            else {
                tooltips.add(new OrderedTextTooltipComponent(keyText.asOrderedText()));
            }

        }

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
        return BadgeFactories.KEYBIND;
    }

}
