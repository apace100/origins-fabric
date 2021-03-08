package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.Comparison;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class BiomeConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Origins.identifier("constant"), new SerializableData()
            .add("value", SerializableDataType.BOOLEAN),
            (data, fluid) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Origins.identifier("and"), new SerializableData()
            .add("conditions", SerializableDataType.BIOME_CONDITIONS),
            (data, fluid) -> ((List<ConditionFactory<Biome>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(fluid)
            )));
        register(new ConditionFactory<>(Origins.identifier("or"), new SerializableData()
            .add("conditions", SerializableDataType.BIOME_CONDITIONS),
            (data, fluid) -> ((List<ConditionFactory<Biome>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(fluid)
            )));

        register(new ConditionFactory<>(Origins.identifier("high_humidity"), new SerializableData(),
            (data, biome) -> biome.hasHighHumidity()));
        register(new ConditionFactory<>(Origins.identifier("temperature"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.FLOAT),
            (data, biome) -> ((Comparison)data.get("comparison")).compare(biome.getTemperature(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("category"), new SerializableData()
            .add("category", SerializableDataType.STRING),
            (data, biome) -> biome.getCategory().getName().equals(data.getString("category"))));
        register(new ConditionFactory<>(Origins.identifier("precipitation"), new SerializableData()
            .add("precipitation", SerializableDataType.STRING),
            (data, biome) -> biome.getPrecipitation().getName().equals(data.getString("precipitation"))));
    }

    private static void register(ConditionFactory<Biome> conditionFactory) {
        Registry.register(ModRegistries.BIOME_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
