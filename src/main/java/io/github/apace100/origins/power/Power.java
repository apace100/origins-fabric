package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class Power {

    protected PlayerEntity player;
    protected PowerType<?> type;

    private List<Predicate<PlayerEntity>> conditions;

    public Power(PowerType<?> type, PlayerEntity player) {
        this.player = player;
        this.conditions = new LinkedList<>();
    }

    public Power addCondition(Predicate<PlayerEntity> condition) {
        this.conditions.add(condition);
        return this;
    }

    public void onChosen() {

    }

    public void onAdded() {

    }

    public void onRemoved() {

    }

    public boolean isActive() {
        return conditions.stream().allMatch(condition -> condition.test(player));
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
