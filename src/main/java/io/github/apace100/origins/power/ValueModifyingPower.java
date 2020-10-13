package io.github.apace100.origins.power;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

import java.util.LinkedList;
import java.util.List;

public class ValueModifyingPower extends Power {

    private final List<EntityAttributeModifier> modifiers = new LinkedList<>();

    public ValueModifyingPower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }

    public void addModifier(EntityAttributeModifier modifier) {
        this.modifiers.add(modifier);
    }

    public List<EntityAttributeModifier> getModifiers() {
        return modifiers;
    }
}
