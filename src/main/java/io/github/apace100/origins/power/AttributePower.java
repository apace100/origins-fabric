package io.github.apace100.origins.power;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

import java.util.LinkedList;
import java.util.List;

public class AttributePower extends Power {

    private final List<Mod> modifiers = new LinkedList<Mod>();

    public AttributePower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }

    public AttributePower(PowerType<?> type, PlayerEntity player, EntityAttribute attribute, EntityAttributeModifier modifier) {
        this(type, player);
        addModifier(attribute, modifier);
    }

    public AttributePower addModifier(EntityAttribute attribute, EntityAttributeModifier modifier) {
        Mod mod = new Mod();
        mod.attribute = attribute;
        mod.modifier = modifier;
        this.modifiers.add(mod);
        return this;
    }

    @Override
    public void onAdded() {
        modifiers.forEach(mod -> {
            if(player.getAttributes().hasAttribute(mod.attribute)) {
                player.getAttributeInstance(mod.attribute).addTemporaryModifier(mod.modifier);
            }
        });

    }

    @Override
    public void onRemoved() {
        modifiers.forEach(mod -> {
            if (player.getAttributes().hasAttribute(mod.attribute)) {
                player.getAttributeInstance(mod.attribute).removeModifier(mod.modifier);
            }
        });
    }

    private static class Mod {
        public EntityAttribute attribute;
        public EntityAttributeModifier modifier;
    }
}
