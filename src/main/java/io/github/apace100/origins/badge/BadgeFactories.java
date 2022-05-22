package io.github.apace100.origins.badge;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.Origins;

public final class BadgeFactories {

    public static final BadgeFactory SPRITE = new BadgeFactory(Origins.identifier("sprite"),
        new SerializableData()
            .add("sprite", SerializableDataTypes.IDENTIFIER),
        SpriteBadge::new);

    public static final BadgeFactory TOOLTIP = new BadgeFactory(Origins.identifier("tooltip"),
        new SerializableData()
            .add("sprite", SerializableDataTypes.IDENTIFIER)
            .add("text", SerializableDataTypes.TEXT),
        TooltipBadge::new);

    // Added mostly for backwards-compatibility as the default factory.
    public static final BadgeFactory KEYBIND = new BadgeFactory(Origins.identifier("keybind"),
        new SerializableData()
            .add("sprite", SerializableDataTypes.IDENTIFIER)
            .add("text", SerializableDataTypes.STRING),
        KeybindBadge::new);

    public static final BadgeFactory CRAFTING_RECIPE = new BadgeFactory(Origins.identifier("crafting_recipe"),
        new SerializableData()
            .add("sprite", SerializableDataTypes.IDENTIFIER)
            .add("recipe", SerializableDataTypes.RECIPE)
            .add("prefix", SerializableDataTypes.TEXT, null)
            .add("suffix", SerializableDataTypes.TEXT, null),
        CraftingRecipeBadge::new);
}
