package io.github.apace100.origins.screen.badge;

import com.google.gson.JsonObject;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.PowerKeyManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

public class KeyBindingBadge extends TooltipBadge {
    private static final Identifier ID = Origins.identifier("key_binding");
    private final String keyId;

    public KeyBindingBadge(Identifier spriteLocation, String rawText, String keyId) {
        super(spriteLocation, rawText);
        this.keyId = keyId;
    }

    @Override
    public List<Text> getTooltipText() {
        List<Text> texts = new LinkedList<>();
        Text keyName = new LiteralText("[").append(KeyBinding.getLocalizedName(keyId).get()).append("]");
        for(String text : rawText.split("\n")) {
            texts.add(new TranslatableText(text, keyName));
        }
        return texts;
    }

    public static BadgeFactory<KeyBindingBadge> keyBindingBadgeFactory() {
        SerializableData data = new SerializableData()
            .add("sprite", SerializableDataTypes.IDENTIFIER)
            .add("text", SerializableDataTypes.STRING, "")
            .add("key", SerializableDataTypes.STRING, "");
        return new BadgeFactory<>(
            ID, data,
            (powerType, jsonObject) -> {
                Identifier powerId = powerType.getIdentifier();
                String key = PowerKeyManager.getKeyIdentifier(powerId);
                if(key.isEmpty()) Origins.LOGGER.warn("Trying to create key binding badge for power " + powerId + " which doesn't have any key binding!");
                SerializableData.Instance instance = data.read(jsonObject);
                instance.set("key", key);
                return instance;
            },
            instance -> new KeyBindingBadge(
                instance.getId("sprite"),
                instance.getString("text"),
                instance.getString("key")
            )
        );
    }

}
