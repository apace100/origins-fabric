package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;

public class WalkOnFluidPower extends Power {

    private final Tag<Fluid> fluidTag;

    public WalkOnFluidPower(PowerType<?> type, PlayerEntity player, Tag<Fluid> fluidTag) {
        super(type, player);
        this.fluidTag = fluidTag;
    }

    public Tag<Fluid> getFluidTag() {
        return fluidTag;
    }
}
