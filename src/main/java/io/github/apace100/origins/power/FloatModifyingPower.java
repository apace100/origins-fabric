package io.github.apace100.origins.power;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

public class FloatModifyingPower extends Power {

    private final EntityAttributeModifier modifier;

    public FloatModifyingPower(PowerType<?> type, PlayerEntity player, EntityAttributeModifier modifier) {
        super(type, player);
        this.modifier = modifier;
    }

    public float apply(float base, float current) {
        switch(modifier.getOperation()) {
            case ADDITION:
                current += modifier.getValue();
                break;
            case MULTIPLY_BASE:
                current += base * modifier.getValue();
                break;
            case MULTIPLY_TOTAL:
                current *= modifier.getValue();
                break;
        }
        return current;
    }
}
