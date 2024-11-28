package io.github.apace100.origins.power.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.ActionOnCallbackPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OriginsActionOnCallbackPowerType extends ActionOnCallbackPowerType {

    public static final TypedDataObjectFactory<OriginsActionOnCallbackPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        ActionOnCallbackPowerType.DATA_FACTORY.getSerializableData().copy()
            .add("entity_action_chosen", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("execute_chosen_when_orb", SerializableDataTypes.BOOLEAN, true),
        (data, condition) -> new OriginsActionOnCallbackPowerType(
            data.get("entity_action_chosen"),
            data.get("execute_chosen_when_orb"),
            data.get("entity_action_respawned"),
            data.get("entity_action_removed"),
            data.get("entity_action_gained"),
            data.get("entity_action_lost"),
            data.get("entity_action_added"),
            condition
        ),
        (powerType, serializableData) -> ActionOnCallbackPowerType.DATA_FACTORY.toData(powerType, serializableData)
            .set("entity_action_chosen", powerType.entityActionChosen)
            .set("execute_chosen_when_orb", powerType.executeChosenWhenOrb)
    );

    private final Optional<EntityAction> entityActionChosen;
    private final boolean executeChosenWhenOrb;

    public OriginsActionOnCallbackPowerType(Optional<EntityAction> entityActionChosen, boolean executeChosenWhenOrb, Optional<EntityAction> entityActionRespawned, Optional<EntityAction> entityActionRemoved, Optional<EntityAction> entityActionGained, Optional<EntityAction> entityActionLost, Optional<EntityAction> entityActionAdded, Optional<EntityCondition> condition) {
        super(entityActionRespawned, entityActionRemoved, entityActionGained, entityActionLost, entityActionAdded, condition);
        this.entityActionChosen = entityActionChosen;
        this.executeChosenWhenOrb = executeChosenWhenOrb;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return OriginsPowerTypes.ACTION_ON_CALLBACK;
    }

    public void onChosen(boolean orbOfOrigins) {

        if (this.isActive() && (!orbOfOrigins || executeChosenWhenOrb)) {
            entityActionChosen.ifPresent(action -> action.execute(getHolder()));
        }

    }

}
