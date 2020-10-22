package io.github.apace100.origins.power.factory.action;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.factory.condition.ConditionFactory;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Random;

public class BlockActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ActionFactory<>(Origins.identifier("and"), new SerializableData()
            .add("actions", SerializableDataType.BLOCK_ACTIONS),
            (data, block) -> ((List<ActionFactory<Triple<World, BlockPos, Direction>>.Instance>)data.get("actions")).forEach((e) -> e.accept(block))));
        register(new ActionFactory<>(Origins.identifier("chance"), new SerializableData()
            .add("action", SerializableDataType.BLOCK_ACTION)
            .add("chance", SerializableDataType.FLOAT),
            (data, block) -> {
                if(new Random().nextFloat() < data.getFloat("chance")) {
                    ((ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("action")).accept(block);
                }
            }));
        register(new ActionFactory<>(Origins.identifier("if_else"), new SerializableData()
            .add("condition", SerializableDataType.BLOCK_CONDITION)
            .add("if_action", SerializableDataType.BLOCK_ACTION)
            .add("else_action", SerializableDataType.BLOCK_ACTION, null),
            (data, block) -> {
                if(((ConditionFactory<CachedBlockPosition>.Instance)data.get("condition")).test(new CachedBlockPosition(block.getLeft(), block.getMiddle(), true))) {
                    ((ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("if_action")).accept(block);
                } else {
                    if(data.isPresent("else_action")) {
                        ((ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("else_action")).accept(block);
                    }
                }
            }));

        register(new ActionFactory<>(Origins.identifier("set_block"), new SerializableData()
            .add("block", SerializableDataType.BLOCK),
            (data, block) -> {
                block.getLeft().setBlockState(block.getMiddle(), ((Block)data.get("block")).getDefaultState());
            }));
        register(new ActionFactory<>(Origins.identifier("add_block"), new SerializableData()
            .add("block", SerializableDataType.BLOCK),
            (data, block) -> {
                block.getLeft().setBlockState(block.getMiddle().offset(block.getRight()), ((Block)data.get("block")).getDefaultState());
            }));
    }

    private static void register(ActionFactory<Triple<World, BlockPos, Direction>> actionFactory) {
        Registry.register(ModRegistries.BLOCK_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
