package io.github.apace100.origins.power;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

public class AttributePower extends Power {

    private final EntityAttribute attribute;
    private final EntityAttributeModifier modifier;

    public AttributePower(PowerType<?> type, PlayerEntity player, EntityAttribute attribute, EntityAttributeModifier modifier) {
        super(type, player);
        this.attribute = attribute;
        this.modifier = modifier;
    }

    @Override
    public void onAdded() {
        if(player.getAttributes().hasAttribute(attribute)) {
            player.getAttributeInstance(attribute).addTemporaryModifier(modifier);
        }
    }

    @Override
    public void onRemoved() {
        if(player.getAttributes().hasAttribute(attribute)) {
            player.getAttributeInstance(attribute).removeModifier(modifier);
        }
    }
}
