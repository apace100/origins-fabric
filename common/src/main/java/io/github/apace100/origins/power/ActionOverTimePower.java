package io.github.apace100.origins.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Consumer;

public class ActionOverTimePower extends Power {

    private final int interval;
    private final Consumer<Entity> entityAction;

    public ActionOverTimePower(PowerType<?> type, PlayerEntity player, int interval, Consumer<Entity> entityAction) {
        super(type, player);
        this.interval = interval;
        this.entityAction = entityAction;
        this.setTicking();
    }

    public void tick() {
        if(player.age % interval == 0) {
            entityAction.accept(player);
        }
    }
}
