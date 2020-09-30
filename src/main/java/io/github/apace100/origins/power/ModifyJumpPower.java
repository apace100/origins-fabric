package io.github.apace100.origins.power;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

public class ModifyJumpPower extends FloatModifyingPower {

    public ModifyJumpPower(PowerType<?> type, PlayerEntity player, EntityAttributeModifier modifier) {
        super(type, player, modifier);
    }
}
