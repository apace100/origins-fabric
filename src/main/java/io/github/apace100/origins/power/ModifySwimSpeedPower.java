package io.github.apace100.origins.power;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

public class ModifySwimSpeedPower extends FloatModifyingPower {

    public ModifySwimSpeedPower(PowerType<?> type, PlayerEntity player, EntityAttributeModifier modifier) {
        super(type, player, modifier);
    }
}
