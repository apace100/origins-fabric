package io.github.apace100.origins.power.factory.action;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.power.factory.condition.ConditionFactory;
import io.github.apace100.origins.registry.ModRegistries;
import io.github.apace100.origins.util.FilterableWeightedList;
import io.github.apace100.origins.util.SerializableData;
import io.github.apace100.origins.util.SerializableDataType;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
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
        register(new ActionFactory<>(Origins.identifier("choice"), new SerializableData()
            .add("actions", SerializableDataType.weightedList(SerializableDataType.BLOCK_ACTION)),
            (data, block) -> {
                FilterableWeightedList<ActionFactory<Triple<World, BlockPos, Direction>>.Instance> actionList = (FilterableWeightedList<ActionFactory<Triple<World, BlockPos, Direction>>.Instance>)data.get("actions");
                ActionFactory<Triple<World, BlockPos, Direction>>.Instance action = actionList.pickRandom(new Random());
                action.accept(block);
            }));
        register(new ActionFactory<>(Origins.identifier("offset"), new SerializableData()
            .add("action", SerializableDataType.BLOCK_ACTION)
            .add("x", SerializableDataType.INT, 0)
            .add("y", SerializableDataType.INT, 0)
            .add("z", SerializableDataType.INT, 0),
            (data, block) -> ((ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("action")).accept(Triple.of(
                block.getLeft(),
                block.getMiddle().add(data.getInt("x"), data.getInt("y"), data.getInt("z")),
                block.getRight())
            )));

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
        register(new ActionFactory<>(Origins.identifier("execute_command"), new SerializableData()
            .add("command", SerializableDataType.STRING)
            .add("permission_level", SerializableDataType.INT, 4),
            (data, block) -> {
                MinecraftServer server = block.getLeft().getServer();
                if(server != null) {
                    String blockName = block.getLeft().getBlockState(block.getMiddle()).getBlock().getTranslationKey();
                    ServerCommandSource source = new ServerCommandSource(
                        CommandOutput.DUMMY,
                        new Vec3d(block.getMiddle().getX() + 0.5, block.getMiddle().getY() + 0.5, block.getMiddle().getZ() + 0.5),
                        new Vec2f(0, 0),
                        (ServerWorld)block.getLeft(),
                        data.getInt("permission_level"),
                        blockName,
                        new TranslatableText(blockName),
                        server,
                        null);
                    server.getCommandManager().execute(source, data.getString("command"));
                }
            }));
    }

    private static void register(ActionFactory<Triple<World, BlockPos, Direction>> actionFactory) {
        Registry.register(ModRegistries.BLOCK_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
