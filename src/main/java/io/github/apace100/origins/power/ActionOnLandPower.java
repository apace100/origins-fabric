package io.github.apace100.origins.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Consumer;

public class ActionOnLandPower extends Power {

    private final Consumer<Entity> entityAction;

    public ActionOnLandPower(PowerType<?> type, PlayerEntity player, Consumer<Entity> entityAction) {
        super(type, player);
        this.entityAction = entityAction;
    }

    public void executeAction() {
        entityAction.accept(player);
    }
}
