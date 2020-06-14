package io.github.apace100.origins.power;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class Power {

    protected PlayerEntity player;
    protected PowerType<?> type;

    public Power(PowerType<?> type, PlayerEntity player) {
        this.player = player;
    }

    public void onAdded() {

    }

    public void onRemoved() {

    }

    public Tag toTag() {
        return new CompoundTag();
    }

    public void fromTag(Tag tag) {

    }

    public PowerType<?> getType() {
        return type;
    }
}
