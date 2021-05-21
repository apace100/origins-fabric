package io.github.apace100.origins.command;

import com.mojang.brigadier.CommandDispatcher;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OriginCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			literal("origin").requires(cs -> cs.hasPermissionLevel(2))
				.then(literal("set")
					.then(argument("targets", EntityArgumentType.players())
					.then(argument("layer", LayerArgument.layer())
					.then(argument("origin", OriginArgument.origin())
					.executes((command) -> {
						// Sets the origins of several people in the given layer.
						int i = 0;
						Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(command, "targets");
						OriginLayer l = command.getArgument("layer", OriginLayer.class);
						Origin o = command.getArgument("origin", Origin.class);
						for(ServerPlayerEntity target : targets) {
							setOrigin(target, l, o);
							i++;
						}
						if (targets.size() == 1) {
							command.getSource().sendFeedback(new TranslatableText("commands.origin.set.success.single", targets.iterator().next().getDisplayName(), new TranslatableText(l.getTranslationKey()), o.getName()), true);
						} else {
							command.getSource().sendFeedback(new TranslatableText("commands.origin.set.success.multiple", targets.size(), new TranslatableText(l.getTranslationKey()), o.getName()), true);
						}
						return i;
					}))))
				)
				.then(literal("has")
					.then(literal("origin")
						.then(argument("targets", EntityArgumentType.players())
						.then(argument("layer", LayerArgument.layer())
						.then(argument("origin", OriginArgument.origin())
						.executes((command) -> {
							// Returns the number of people in the target selector with the origin in the given layer.
							// Useful for checking if a player has the given origin in functions.
							int i = 0;
							Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(command, "targets");
							OriginLayer l = command.getArgument("layer", OriginLayer.class);
							Origin o = command.getArgument("origin", Origin.class);
							for(ServerPlayerEntity target : targets) {
								if (hasOrigin(target, l, o)) {
									i++;
								}
							}
							if (i == 0) {
								command.getSource().sendError(new TranslatableText("commands.execute.conditional.fail"));
							} else if (targets.size() == 1) {
								command.getSource().sendFeedback(new TranslatableText("commands.execute.conditional.pass"), false);
							} else {
								command.getSource().sendFeedback(new TranslatableText("commands.execute.conditional.pass_count", i), false);
							}
							return i;
						}))))
					)
					.then(literal("power")
						.then(argument("targets", EntityArgumentType.players())
						.then(argument("power", PowerArgument.power())
						.executes((command) -> {
							// Returns the number of people in the target selector with the given power.
							// Useful for checking if a player has the given power in functions.
							int i = 0;
							Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(command, "targets");
							PowerType<?> powerType = command.getArgument("power", PowerType.class);
							for(ServerPlayerEntity target : targets) {
								if (hasPower(target, powerType)) {
									i++;
								}
							}
							if (i == 0) {
								command.getSource().sendError(new TranslatableText("commands.execute.conditional.fail"));
							} else if (targets.size() == 1) {
								command.getSource().sendFeedback(new TranslatableText("commands.execute.conditional.pass"), false);
							} else {
								command.getSource().sendFeedback(new TranslatableText("commands.execute.conditional.pass_count", i), false);
							}
							return i;
						})))
					)
				)
			.then(literal("get")
				.then(argument("target", EntityArgumentType.player())
					.then(argument("layer", LayerArgument.layer())
						.executes((command) -> {
							ServerPlayerEntity target = EntityArgumentType.getPlayer(command, "target");
							OriginLayer layer = command.getArgument("layer", OriginLayer.class);
							OriginComponent component = ModComponents.ORIGIN.get(target);
							Origin origin = component.getOrigin(layer);
							command.getSource().sendFeedback(new TranslatableText("commands.origin.get.result", target.getDisplayName(), new TranslatableText(layer.getTranslationKey()), origin.getName(), origin.getIdentifier()), false);
							return 1;
						})
					)
				)
			)
		);
	}

	private static void setOrigin(PlayerEntity player, OriginLayer layer, Origin origin) {
		OriginComponent component = ModComponents.ORIGIN.get(player);
		component.setOrigin(layer, origin);
		OriginComponent.sync(player);
		boolean hadOriginBefore = component.hadOriginBefore();
		origin.getPowerTypes().forEach(powerType -> component.getPower(powerType).onChosen(hadOriginBefore));
	}

	private static boolean hasOrigin(PlayerEntity player, OriginLayer layer, Origin origin) {
		OriginComponent component = ModComponents.ORIGIN.get(player);
		return component.hasOrigin(layer) && component.getOrigin(layer).equals(origin);
	}

	private static boolean hasPower(PlayerEntity player, PowerType<?> powerType) {
		return ModComponents.ORIGIN.get(player).hasPower(powerType);
	}
}
