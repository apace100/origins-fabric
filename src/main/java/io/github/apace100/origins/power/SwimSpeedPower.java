package io.github.apace100.origins.power;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

public class SwimSpeedPower extends FloatModifyingPower {

    public SwimSpeedPower(PowerType<?> type, PlayerEntity player, EntityAttributeModifier modifier) {
        super(type, player, modifier);
    }
}
