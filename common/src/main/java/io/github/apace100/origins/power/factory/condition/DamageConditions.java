package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.Comparison;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class DamageConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Origins.identifier("constant"), new SerializableData()
            .add("value", SerializableDataType.BOOLEAN),
            (data, dmg) -> data.getBoolean("value")));
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
        register(new ConditionFactory<>(Origins.identifier("fire"), new SerializableData(),
            (data, dmg) -> dmg.getLeft().isFire()));
        register(new ConditionFactory<>(Origins.identifier("name"), new SerializableData()
            .add("name", SerializableDataType.STRING),
            (data, dmg) -> dmg.getLeft().getName().equals(data.get("name"))));
        register(new ConditionFactory<>(Origins.identifier("projectile"), new SerializableData()
            .add("projectile", SerializableDataType.ENTITY_TYPE, null),
            (data, dmg) -> {
                if(dmg.getLeft() instanceof ProjectileDamageSource) {
                    Entity projectile = dmg.getLeft().getSource();
                    return projectile != null && (!data.isPresent("projectile") || projectile.getType() == data.get("projectile"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Origins.identifier("attacker"), new SerializableData()
            .add("entity_condition", SerializableDataType.ENTITY_CONDITION, null),
            (data, dmg) -> {
                Entity attacker = dmg.getLeft().getAttacker();
                if(attacker instanceof LivingEntity) {
                    if(!data.isPresent("entity_condition") || ((ConditionFactory<LivingEntity>.Instance)data.get("entity_condition")).test((LivingEntity)attacker)) {
                        return true;
                    }
                }
                return false;
            }));
    }

    private static void register(ConditionFactory<Pair<DamageSource, Float>> conditionFactory) {
        ModRegistries.DAMAGE_CONDITION.register(conditionFactory.getSerializerId(), () -> conditionFactory);
    }
}
