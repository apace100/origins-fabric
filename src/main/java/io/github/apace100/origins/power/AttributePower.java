package io.github.apace100.origins.power;

import io.github.apace100.origins.util.AttributedEntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

import java.util.LinkedList;
import java.util.List;

public class AttributePower extends Power {

    private final List<AttributedEntityAttributeModifier> modifiers = new LinkedList<AttributedEntityAttributeModifier>();

    public AttributePower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }

    public AttributePower(PowerType<?> type, PlayerEntity player, EntityAttribute attribute, EntityAttributeModifier modifier) {
        this(type, player);
        addModifier(attribute, modifier);
    }

    public AttributePower addModifier(EntityAttribute attribute, EntityAttributeModifier modifier) {
        AttributedEntityAttributeModifier mod = new AttributedEntityAttributeModifier(attribute, modifier);
        this.modifiers.add(mod);
        return this;
    }

    public AttributePower addModifier(AttributedEntityAttributeModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    @Override
    public void onAdded() {
        modifiers.forEach(mod -> {
            if(player.getAttributes().hasAttribute(mod.getAttribute())) {
                player.getAttributeInstance(mod.getAttribute()).addTemporaryModifier(mod.getModifier());
            }
        });
    }

    @Override
    public void onRemoved() {
        modifiers.forEach(mod -> {
            if (player.getAttributes().hasAttribute(mod.getAttribute())) {
                player.getAttributeInstance(mod.getAttribute()).removeModifier(mod.getModifier());
            }
        });
    }
}
