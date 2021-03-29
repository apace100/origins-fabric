package io.github.apace100.origins.power.factory.condition;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.Comparison;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.Property;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.LightType;

import java.util.Collection;
import java.util.List;

public class BlockConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Origins.identifier("constant"), new SerializableData()
            .add("value", SerializableDataType.BOOLEAN),
            (data, block) -> data.getBoolean("value")));
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
        register(new ConditionFactory<>(Origins.identifier("offset"), new SerializableData()
            .add("condition", SerializableDataType.BLOCK_CONDITION)
            .add("x", SerializableDataType.INT, 0)
            .add("y", SerializableDataType.INT, 0)
            .add("z", SerializableDataType.INT, 0),
            (data, block) -> ((ConditionFactory<CachedBlockPosition>.Instance)data.get("condition"))
                .test(new CachedBlockPosition(
                    block.getWorld(),
                    block.getBlockPos().add(
                        data.getInt("x"),
                        data.getInt("y"),
                        data.getInt("z")
                    ), true))));

        register(new ConditionFactory<>(Origins.identifier("height"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT),
            (data, block) -> ((Comparison)data.get("comparison")).compare(block.getBlockPos().getY(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Origins.identifier("block"), new SerializableData()
            .add("block", SerializableDataType.BLOCK),
            (data, block) -> block.getBlockState().isOf((Block)data.get("block"))));
        register(new ConditionFactory<>(Origins.identifier("in_tag"), new SerializableData()
            .add("tag", SerializableDataType.BLOCK_TAG),
            (data, block) -> {
                if(block == null || block.getBlockState() == null) {
                    return false;
                }
                return block.getBlockState().isIn((Tag<Block>)data.get("tag"));
            }));
        register(new ConditionFactory<>(Origins.identifier("adjacent"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT)
            .add("adjacent_condition", SerializableDataType.BLOCK_CONDITION),
            (data, block) -> {
                ConditionFactory<CachedBlockPosition>.Instance adjacentCondition = (ConditionFactory<CachedBlockPosition>.Instance)data.get("adjacent_condition");
                int adjacent = 0;
                for(Direction d : Direction.values()) {
                    if(adjacentCondition.test(new CachedBlockPosition(block.getWorld(), block.getBlockPos().offset(d), true))) {
                        adjacent++;
                    }
                }
                return ((Comparison)data.get("comparison")).compare(adjacent, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Origins.identifier("replacable"), new SerializableData(),
            (data, block) -> block.getBlockState().getMaterial().isReplaceable()));
        register(new ConditionFactory<>(Origins.identifier("attachable"), new SerializableData(),
            (data, block) -> {
                for(Direction d : Direction.values()) {
                    BlockPos adjacent = block.getBlockPos().offset(d);
                    if(block.getWorld().getBlockState(adjacent).isSideSolidFullSquare(block.getWorld(), block.getBlockPos(), d.getOpposite())) {
                        return true;
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Origins.identifier("fluid"), new SerializableData()
            .add("fluid_condition", SerializableDataType.FLUID_CONDITION),
            (data, block) -> ((ConditionFactory<FluidState>.Instance)data.get("fluid_condition")).test(block.getWorld().getFluidState(block.getBlockPos()))));
        register(new ConditionFactory<>(Origins.identifier("movement_blocking"), new SerializableData(),
            (data, block) -> block.getBlockState().getMaterial().blocksMovement() && !block.getBlockState().getCollisionShape(block.getWorld(), block.getBlockPos()).isEmpty()));
        register(new ConditionFactory<>(Origins.identifier("light_blocking"), new SerializableData(),
            (data, block) -> block.getBlockState().getMaterial().blocksLight()));
        register(new ConditionFactory<>(Origins.identifier("water_loggable"), new SerializableData(),
            (data, block) -> block.getBlockState().getBlock() instanceof FluidFillable));
        register(new ConditionFactory<>(Origins.identifier("exposed_to_sky"), new SerializableData(),
            (data, block) -> block.getWorld().isSkyVisible(block.getBlockPos())));
        register(new ConditionFactory<>(Origins.identifier("light_level"), new SerializableData()
            .add("comparison", SerializableDataType.COMPARISON)
            .add("compare_to", SerializableDataType.INT)
            .add("light_type", SerializableDataType.enumValue(LightType.class), null),
            (data, block) -> {
                int value;
                if(data.isPresent("light_type")) {
                    LightType lightType = (LightType)data.get("light_type");
                    value = block.getWorld().getLightLevel(lightType, block.getBlockPos());
                } else {
                    value = block.getWorld().getLightLevel(block.getBlockPos());
                }
                return ((Comparison)data.get("comparison")).compare(value, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Origins.identifier("block_state"), new SerializableData()
            .add("property", SerializableDataType.STRING)
            .add("comparison", SerializableDataType.COMPARISON, null)
            .add("compare_to", SerializableDataType.INT, null)
            .add("value", SerializableDataType.BOOLEAN, null)
            .add("enum", SerializableDataType.STRING, null),
            (data, block) -> {
                BlockState state = block.getBlockState();
                Collection<Property<?>> properties = state.getProperties();
                String desiredPropertyName = data.getString("property");
                Property<?> property = null;
                for(Property<?> p : properties) {
                    if(p.getName().equals(desiredPropertyName)) {
                        property = p;
                        break;
                    }
                }
                if(property != null) {
                    Object value = state.get(property);
                    if(data.isPresent("enum") && value instanceof Enum) {
                        return ((Enum)value).name().equalsIgnoreCase(data.getString("enum"));
                    } else if(data.isPresent("value") && value instanceof Boolean) {
                        return (Boolean) value == data.getBoolean("value");
                    } else if(data.isPresent("comparison") && data.isPresent("compare_to") && value instanceof Integer) {
                        return ((Comparison)data.get("comparison")).compare((Integer) value, data.getInt("compare_to"));
                    }
                }
                return false;
            }));
    }

    private static void register(ConditionFactory<CachedBlockPosition> conditionFactory) {
        Registry.register(ModRegistries.BLOCK_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
