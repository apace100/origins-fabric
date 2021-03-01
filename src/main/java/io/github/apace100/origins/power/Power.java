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

    private boolean shouldTick = false;
    private boolean shouldTickWhenInactive = false;

    private List<Predicate<PlayerEntity>> conditions;

    public Power(PowerType<?> type, PlayerEntity player) {
        this.type = type;
        this.player = player;
        this.conditions = new LinkedList<>();
    }

    public Power addCondition(Predicate<PlayerEntity> condition) {
        this.conditions.add(condition);
        return this;
    }

    protected void setTicking() {
        this.setTicking(false);
    }

    protected void setTicking(boolean evenWhenInactive) {
        this.shouldTick = true;
        this.shouldTickWhenInactive = evenWhenInactive;
    }

    public boolean shouldTick() {
        return shouldTick;
    }

    public boolean shouldTickWhenInactive() {
        return shouldTickWhenInactive;
    }

    public void tick() {

    }

    public void onChosen(boolean isOrbOfOrigin) {

    }

    public void onLost() {

    }

    public void onAdded() {

    }

    public void onRemoved() {

    }

    public void onRespawn() {

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
