package io.github.apace100.origins.power;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.ActionOnCallbackPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.Origins;
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

        if (this.isActive() && entityActionChosen != null && (!isOrbOfOrigins || executeChosenWhenOrb)) {
            entityActionChosen.accept(entity);
        }

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Origins.identifier("action_on_callback"),
            new SerializableData()
                .add("entity_action_respawned", ApoliDataTypes.ENTITY_ACTION, null)
                .add("entity_action_removed", ApoliDataTypes.ENTITY_ACTION, null)
                .add("entity_action_gained", ApoliDataTypes.ENTITY_ACTION, null)
                .add("entity_action_lost", ApoliDataTypes.ENTITY_ACTION, null)
                .add("entity_action_added", ApoliDataTypes.ENTITY_ACTION, null)
                .add("entity_action_chosen", ApoliDataTypes.ENTITY_ACTION, null)
                .add("execute_chosen_when_orb", SerializableDataTypes.BOOLEAN, true),
            data -> (powerType, livingEntity) -> new OriginsCallbackPower(
                powerType,
                livingEntity,
                data.get("entity_action_respawned"),
                data.get("entity_action_removed"),
                data.get("entity_action_gained"),
                data.get("entity_action_lost"),
                data.get("entity_action_added"),
                data.get("entity_action_chosen"),
                data.get("execute_chosen_when_orb")
            )
        ).allowCondition();
    }

}
