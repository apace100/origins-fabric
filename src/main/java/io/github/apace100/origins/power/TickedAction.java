package io.github.apace100.origins.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Consumer;

public class TickedAction extends Power {

    private final int tickRate;
    private final Consumer<Entity> entityAction;

    public TickedAction(PowerType<?> type, PlayerEntity player, int tickRate, Consumer<Entity> entityAction) {
        super(type, player);
        this.setTicking();
        this.tickRate = tickRate;
        this.entityAction = entityAction;
    }

    @Override
    public void tick() {
        if(player.age % tickRate == 0) {
            entityAction.accept(player);
        }
    }
}
