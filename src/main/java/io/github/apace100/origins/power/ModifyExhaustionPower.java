package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;

public class ModifyExhaustionPower extends FloatPower {

    public ModifyExhaustionPower(PowerType<?> type, PlayerEntity player, float exhaustionMultiplier) {
        super(type, player, exhaustionMultiplier);
    }
}
