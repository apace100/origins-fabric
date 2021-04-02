package io.github.apace100.origins.power;

import io.github.apace100.origins.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Consumer;

public class ResourcePower extends HudRenderedVariableIntPower {

    private final Consumer<Entity> actionOnMin;
    private final Consumer<Entity> actionOnMax;

    public ResourcePower(PowerType<?> type, PlayerEntity player, HudRender hudRender, int startValue, int min, int max, Consumer<Entity> actionOnMin, Consumer<Entity> actionOnMax) {
        super(type, player, hudRender, startValue, min, max);
        this.actionOnMin = actionOnMin;
        this.actionOnMax = actionOnMax;
    }

    @Override
    public int setValue(int newValue) {
        int oldValue = currentValue;
        int actualNewValue = super.setValue(newValue);
        if(oldValue != actualNewValue) {
            if(actionOnMin != null && actualNewValue == min) {
                actionOnMin.accept(player);
            }
            if(actionOnMax != null && actualNewValue == max) {
                actionOnMax.accept(player);
            }
        }
        return actualNewValue;
    }
}
