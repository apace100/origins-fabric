package io.github.apace100.origins.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Consumer;

public class ActionOnCallbackPower extends Power {

    private final Consumer<Entity> entityActionRespawned;
    private final Consumer<Entity> entityActionRemoved;
    private final Consumer<Entity> entityActionChosen;
    private final Consumer<Entity> entityActionLost;
    private final Consumer<Entity> entityActionAdded;
    private final boolean executeChosenWhenOrb;

    public ActionOnCallbackPower(PowerType<?> type, PlayerEntity player, Consumer<Entity> entityActionRespawned, Consumer<Entity> entityActionRemoved, Consumer<Entity> entityActionChosen, Consumer<Entity> entityActionLost, Consumer<Entity> entityActionAdded, boolean executeChosenWhenOrb) {
        super(type, player);
        this.entityActionRespawned = entityActionRespawned;
        this.entityActionRemoved = entityActionRemoved;
        this.entityActionChosen = entityActionChosen;
        this.entityActionLost = entityActionLost;
        this.entityActionAdded = entityActionAdded;
        this.executeChosenWhenOrb = executeChosenWhenOrb;
    }

    @Override
    public void onRespawn() {
        if(entityActionRespawned != null) {
            entityActionRespawned.accept(player);
        }
    }

    @Override
    public void onChosen(boolean isOrbOfOrigin) {
        if(entityActionChosen != null && (!isOrbOfOrigin || executeChosenWhenOrb)) {
            entityActionChosen.accept(player);
        }
    }

    @Override
    public void onRemoved() {
        if(entityActionRemoved != null) {
            entityActionRemoved.accept(player);
        }
    }

    @Override
    public void onLost() {
        if(entityActionLost != null) {
            entityActionLost.accept(player);
        }
    }

    @Override
    public void onAdded() {
        if(entityActionAdded != null) {
            entityActionAdded.accept(player);
        }
    }
}
