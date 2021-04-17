package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistriesArchitectury;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.fluid.FluidState;

import java.util.List;

public class FluidConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Origins.identifier("constant"), new SerializableData()
            .add("value", SerializableDataType.BOOLEAN),
            (data, fluid) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Origins.identifier("and"), new SerializableData()
            .add("conditions", SerializableDataType.FLUID_CONDITIONS),
            (data, fluid) -> ((List<ConditionFactory<FluidState>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(fluid)
            )));
        register(new ConditionFactory<>(Origins.identifier("or"), new SerializableData()
            .add("conditions", SerializableDataType.FLUID_CONDITIONS),
            (data, fluid) -> ((List<ConditionFactory<FluidState>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(fluid)
            )));

        register(new ConditionFactory<>(Origins.identifier("empty"), new SerializableData(),
            (data, fluid) -> fluid.isEmpty()));
        register(new ConditionFactory<>(Origins.identifier("still"), new SerializableData(),
            (data, fluid) -> fluid.isStill()));
        register(new ConditionFactory<>(Origins.identifier("in_tag"), new SerializableData()
            .add("tag", SerializableDataType.FLUID_TAG),
            (data, fluid) -> fluid.isIn(data.get("tag"))));
    }

    private static void register(ConditionFactory<FluidState> conditionFactory) {
        ModRegistriesArchitectury.FLUID_CONDITION.register(conditionFactory.getSerializerId(), () -> conditionFactory);
    }
}
