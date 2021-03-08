package io.github.apace100.origins.power;

import io.github.apace100.origins.util.AttributedEntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;

import java.util.LinkedList;
import java.util.List;

public class ConditionedAttributePower extends Power {

    private final List<AttributedEntityAttributeModifier> modifiers = new LinkedList<AttributedEntityAttributeModifier>();
    private final int tickRate;

    public ConditionedAttributePower(PowerType<?> type, PlayerEntity player, int tickRate) {
        super(type, player);
        this.setTicking(true);
        this.tickRate = tickRate;
    }

    @Override
    public void tick() {
        if(player.age % tickRate == 0) {
            if(this.isActive()) {
                addMods();
            } else {
                removeMods();
            }
        }
    }

    @Override
    public void onRemoved() {
        removeMods();
    }

    public ConditionedAttributePower addModifier(AttributedEntityAttributeModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public void addMods() {
        modifiers.forEach(mod -> {
            if(player.getAttributes().hasAttribute(mod.getAttribute())) {
                EntityAttributeInstance instance = player.getAttributeInstance(mod.getAttribute());
                if(instance != null) {
                    if(!instance.hasModifier(mod.getModifier())) {
                        instance.addTemporaryModifier(mod.getModifier());
                    }
                }
            }
        });
    }

    public void removeMods() {
        modifiers.forEach(mod -> {
            if (player.getAttributes().hasAttribute(mod.getAttribute())) {
                EntityAttributeInstance instance = player.getAttributeInstance(mod.getAttribute());
                if(instance != null) {
                    if(instance.hasModifier(mod.getModifier())) {
                        instance.removeModifier(mod.getModifier());
                    }
                }
            }
        });
    }
}
