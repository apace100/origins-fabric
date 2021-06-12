package io.github.apace100.origins.power;

import io.github.apace100.apoli.power.ActionOnCallbackPower;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.function.Consumer;

public class OriginsCallbackPower extends ActionOnCallbackPower {

    private final Consumer<Entity> entityActionChosen;
    private final boolean executeChosenWhenOrb;

    public OriginsCallbackPower(PowerType<?> type, LivingEntity entity, Consumer<Entity> entityActionRespawned, Consumer<Entity> entityActionRemoved, Consumer<Entity> entityActionGained, Consumer<Entity> entityActionLost, Consumer<Entity> entityActionAdded, Consumer<Entity> entityActionChosen, boolean executeChosenWhenOrb) {
        super(type, entity, entityActionRespawned, entityActionRemoved, entityActionGained, entityActionLost, entityActionAdded);
        this.entityActionChosen = entityActionChosen;
        this.executeChosenWhenOrb = executeChosenWhenOrb;
    }

    public void onChosen(boolean isOrbOfOrigins) {
        if(entityActionChosen != null) {
            if(!isOrbOfOrigins || executeChosenWhenOrb) {
                entityActionChosen.accept(entity);
            }
        }
    }
}
