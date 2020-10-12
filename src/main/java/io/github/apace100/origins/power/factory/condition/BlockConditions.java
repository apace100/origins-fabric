package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.Comparison;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class BlockConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Origins.identifier("and"), new SerializableData()
            .add("conditions", SerializableDataType.BLOCK_CONDITIONS),
            (data, block) -> ((List<ConditionFactory<CachedBlockPosition>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(block)
            )));
        register(new ConditionFactory<>(Origins.identifier("or"), new SerializableData()
            .add("conditions", SerializableDataType.BLOCK_CONDITIONS),
            (data, block) -> ((List<ConditionFactory<CachedBlockPosition>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(block)
            )));
        register(new ConditionFactory<>(Origins.identifier("height"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT),
            (data, block) -> ((Comparison)data.get("comparison")).compare(block.getBlockPos().getY(), data.getInt("compare_to"))));

        register(new ConditionFactory<>(Origins.identifier("is_block"), new SerializableData()
            .add("block", SerializableDataType.BLOCK),
            (data, block) -> block.getBlockState().isOf((Block)data.get("block"))));
        register(new ConditionFactory<>(Origins.identifier("is_in_tag"), new SerializableData()
            .add("tag", SerializableDataType.BLOCK_TAG),
            (data, block) -> block.getBlockState().isIn((Tag<Block>)data.get("tag"))));
        register(new ConditionFactory<>(Origins.identifier("adjacent"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT)
            .add("adjacent_condition", SerializableDataType.BLOCK_CONDITION),
            (data, block) -> {
                ConditionFactory<CachedBlockPosition>.Instance adjacentCondition = (ConditionFactory<CachedBlockPosition>.Instance)data.get("adjacent_condition");
                int adjacent = 0;
                for(Direction d : Direction.values()) {
                    if(adjacentCondition.test(new CachedBlockPosition(block.getWorld(), block.getBlockPos().offset(d), false))) {
                        adjacent++;
                    }
                }
                return ((Comparison)data.get("comparison")).compare(adjacent, data.getInt("compare_to"));
            }));
    }

    private static void register(ConditionFactory<CachedBlockPosition> conditionFactory) {
        Registry.register(ModRegistries.BLOCK_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
