package io.github.apace100.origins.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Consumer;

public class TickedActionOnChange extends Power {

    private final int tickRate;
    private final Consumer<Entity> risingAction;
    private final Consumer<Entity> fallingAction;

    private boolean wasActive = false;

    public TickedActionOnChange(PowerType<?> type, PlayerEntity player, int tickRate, Consumer<Entity> risingAction, Consumer<Entity> fallingAction) {
        super(type, player);
        this.setTicking(true);
        this.tickRate = tickRate;
        this.risingAction = risingAction;
        this.fallingAction = fallingAction;
    }

    @Override
    public void tick() {
        if(player.age % tickRate == 0) {
            if (isActive()) {
                if (!wasActive && risingAction != null) {
                    risingAction.accept(player);
                }
                wasActive = true;
            } else {
                if (wasActive && fallingAction != null) {
                    fallingAction.accept(player);
                }
                wasActive = false;
            }
        }
    }
}
