package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.Comparison;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class DamageConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Origins.identifier("and"), new SerializableData()
            .add("conditions", SerializableDataType.DAMAGE_CONDITIONS),
            (data, dmg) -> ((List<ConditionFactory<Pair<DamageSource, Float>>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(dmg)
            )));
        register(new ConditionFactory<>(Origins.identifier("or"), new SerializableData()
            .add("conditions", SerializableDataType.DAMAGE_CONDITIONS),
            (data, dmg) -> ((List<ConditionFactory<Pair<DamageSource, Float>>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(dmg)
            )));
        register(new ConditionFactory<>(Origins.identifier("amount"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.FLOAT),
            (data, dmg) -> ((Comparison)data.get("comparison")).compare(dmg.getRight(), data.getFloat("compare_to"))));

        register(new ConditionFactory<>(Origins.identifier("is_fire"), new SerializableData(),
            (data, dmg) -> dmg.getLeft().isFire()));
        register(new ConditionFactory<>(Origins.identifier("name"), new SerializableData()
            .add("name", SerializableDataType.STRING),
            (data, dmg) -> dmg.getLeft().getName().equals(data.getString("name"))));
    }

    private static void register(ConditionFactory<Pair<DamageSource, Float>> conditionFactory) {
        Registry.register(ModRegistries.DAMAGE_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
