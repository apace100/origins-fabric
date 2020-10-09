package io.github.apace100.origins.util;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;

public class AttributedEntityAttributeModifier {

    private final EntityAttribute attribute;
    private final EntityAttributeModifier modifier;

    public AttributedEntityAttributeModifier(EntityAttribute attribute, EntityAttributeModifier modifier) {
        this.attribute = attribute;
        this.modifier = modifier;
    }

    public EntityAttributeModifier getModifier() {
        return modifier;
    }

    public EntityAttribute getAttribute() {
        return attribute;
    }
}
