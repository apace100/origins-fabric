package io.github.apace100.origins.util;

import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.Comparator;
import java.util.List;

public final class AttributeUtil {

    public static void sortModifiers(List<EntityAttributeModifier> modifiers) {
        modifiers.sort(Comparator.comparing(e -> e.getOperation().getId()));
    }

    public static double sortAndApplyModifiers(List<EntityAttributeModifier> modifiers, double baseValue) {
        sortModifiers(modifiers);
        return applyModifiers(modifiers, baseValue);
    }

    public static double applyModifiers(List<EntityAttributeModifier> modifiers, double baseValue) {
        double currentValue = baseValue;
        if(modifiers != null) {
            for(EntityAttributeModifier modifier : modifiers) {
                switch(modifier.getOperation()) {
                    case ADDITION:
                        currentValue += modifier.getValue();
                        break;
                    case MULTIPLY_BASE:
                        currentValue += baseValue * modifier.getValue();
                        break;
                    case MULTIPLY_TOTAL:
                        currentValue *= (1 + modifier.getValue());
                        break;
                }
            }
        }
        return currentValue;
    }
}
