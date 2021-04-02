package io.github.apace100.origins.power;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

public class VariableIntPower extends Power {

    protected final int min, max;
    protected int currentValue;

    public VariableIntPower(PowerType<?> type, PlayerEntity player, int startValue) {
        this(type, player, startValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public VariableIntPower(PowerType<?> type, PlayerEntity player, int startValue, int min, int max) {
        super(type, player);
        this.currentValue = startValue;
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getValue() {
        return currentValue;
    }

    public int setValue(int newValue) {
        if(newValue > getMax())
            newValue = getMax();
        if(newValue < getMin())
            newValue = getMin();
        return currentValue = newValue;
    }

    public int increment() {
        return setValue(getValue() + 1);
    }

    public int decrement() {
        return setValue(getValue() - 1);
    }

    @Override
    public Tag toTag() {
        return IntTag.of(currentValue);
    }

    @Override
    public void fromTag(Tag tag) {
        currentValue = ((IntTag)tag).getInt();
    }
}
