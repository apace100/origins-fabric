package io.github.apace100.origins.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.CooldownPower;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.VariableIntPower;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ObjectiveArgumentType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ResourceCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("resource").requires(cs -> cs.hasPermissionLevel(2))
                .then(literal("has")
                    .then(argument("target", EntityArgumentType.player())
                        .then(argument("power", PowerArgument.power())
                            .executes((command) -> {return resource(command, Subcommands.HAS);})))
                )
                .then(literal("get")
                    .then(argument("target", EntityArgumentType.player())
                        .then(argument("power", PowerArgument.power())
                            .executes((command) -> {return resource(command, Subcommands.GET);})))
                )
                .then(literal("set")
                    .then(argument("target", EntityArgumentType.player())
                        .then(argument("power", PowerArgument.power())
                            .then(argument("value", IntegerArgumentType.integer())
                                .executes((command) -> {return resource(command, Subcommands.SET);}))))
                )
                .then(literal("change")
                    .then(argument("target", EntityArgumentType.player())
                        .then(argument("power", PowerArgument.power())
                            .then(argument("value", IntegerArgumentType.integer())
                                .executes((command) -> {return resource(command, Subcommands.CHANGE);}))))
                )
                .then(literal("operation")
                    .then(argument("target", EntityArgumentType.player())
                        .then(argument("power", PowerArgument.power())
                            .then(argument("operation", PowerOperation.operation())
                                .then(argument("entity", ScoreHolderArgumentType.scoreHolder())
                                    .then(argument("objective", ObjectiveArgumentType.objective())
                                        .executes((command) -> {return resource(command, Subcommands.OPERATION);}))))))
                )
        );
    }

    public static enum Subcommands {
        HAS, GET, SET, CHANGE, OPERATION
    }

    // This is a cleaner method than sticking it into every subcommand
    private static int resource(CommandContext<ServerCommandSource> command, Subcommands sub) throws CommandSyntaxException {
        int i = 0;

        ServerPlayerEntity player = EntityArgumentType.getPlayer(command, "target");
        PowerType powerType = command.getArgument("power", PowerType.class);
        Power power = ModComponents.ORIGIN.get(player).getPower(powerType);

        if (power instanceof VariableIntPower) {
            VariableIntPower vIntPower = ((VariableIntPower) power);
            switch (sub) {
                case HAS:
                    command.getSource().sendFeedback(new TranslatableText("commands.execute.conditional.pass"), true);
                    return 1;
                case GET:
                    i = vIntPower.getValue();
                    command.getSource().sendFeedback(new TranslatableText("commands.scoreboard.players.get.success", player.getEntityName(), i, powerType.getIdentifier()), true);
                    return i;
                case SET:
                    i = IntegerArgumentType.getInteger(command, "value");
                    vIntPower.setValue(i);
                    OriginComponent.sync(player);
                    command.getSource().sendFeedback(new TranslatableText("commands.scoreboard.players.set.success.single", powerType.getIdentifier(), player.getEntityName(), i), true);
                    return 1;
                case CHANGE:
                    i = IntegerArgumentType.getInteger(command, "value");
                    int total = vIntPower.getValue()+i;
                    vIntPower.setValue(total);
                    OriginComponent.sync(player);
                    command.getSource().sendFeedback(new TranslatableText("commands.scoreboard.players.add.success.single", i, powerType.getIdentifier(), player.getEntityName(), total), true);
                    return 1;
                case OPERATION:
                    ScoreboardPlayerScore score = command.getSource().getMinecraftServer().getScoreboard().getPlayerScore(ScoreHolderArgumentType.getScoreHolder(command, "entity"), ObjectiveArgumentType.getObjective(command, "objective"));
                    command.getArgument("operation", PowerOperation.Operation.class).apply(vIntPower, score);
                    OriginComponent.sync(player);
                    command.getSource().sendFeedback(new TranslatableText("commands.scoreboard.players.operation.success.single", powerType.getIdentifier(), player.getEntityName(), vIntPower.getValue()), true);
                    return 1;
            }
        } else if(power instanceof CooldownPower) {
            CooldownPower cooldownPower = ((CooldownPower) power);
            switch (sub) {
                case HAS:
                    command.getSource().sendFeedback(new TranslatableText("commands.execute.conditional.pass"), true);
                    return 1;
                case GET:
                    i = cooldownPower.getRemainingTicks();
                    command.getSource().sendFeedback(new TranslatableText("commands.scoreboard.players.get.success", player.getEntityName(), i, powerType.getIdentifier()), true);
                    return i;
                case SET:
                    i = IntegerArgumentType.getInteger(command, "value");
                    cooldownPower.setCooldown(i);
                    OriginComponent.sync(player);
                    command.getSource().sendFeedback(new TranslatableText("commands.scoreboard.players.set.success.single", powerType.getIdentifier(), player.getEntityName(), i), true);
                    return 1;
                case CHANGE:
                    i = IntegerArgumentType.getInteger(command, "value");
                    cooldownPower.modify(i);
                    OriginComponent.sync(player);
                    command.getSource().sendFeedback(new TranslatableText("commands.scoreboard.players.add.success.single", i, powerType.getIdentifier(), player.getEntityName(), cooldownPower.getRemainingTicks()), true);
                    return 1;
                case OPERATION:
                    ScoreboardPlayerScore score = command.getSource().getMinecraftServer().getScoreboard().getPlayerScore(ScoreHolderArgumentType.getScoreHolder(command, "entity"), ObjectiveArgumentType.getObjective(command, "objective"));
                    command.getArgument("operation", PowerOperation.Operation.class).apply(cooldownPower, score);
                    OriginComponent.sync(player);
                    command.getSource().sendFeedback(new TranslatableText("commands.scoreboard.players.operation.success.single", powerType.getIdentifier(), player.getEntityName(), cooldownPower.getRemainingTicks()), true);
                    return 1;
            }
        } else {
            switch (sub) {
                case HAS:
                    command.getSource().sendError(new TranslatableText("commands.execute.conditional.fail"));
                    return 0;
                case GET:
                    command.getSource().sendError(new TranslatableText("commands.scoreboard.players.get.null", powerType.getIdentifier(), player.getEntityName()));
                    return 0;
                case SET:
                case CHANGE:
                case OPERATION:
                    // This translation is a bit of a stretch, as it reads "No relevant score holders could be found"
                    command.getSource().sendError(new TranslatableText("argument.scoreHolder.empty"));
                    return 0;
            }
        }
        return 0;
    }
}
