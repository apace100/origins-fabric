package io.github.apace100.origins.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
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
								}))))));
	}

	private static void setOrigin(PlayerEntity player, OriginLayer layer, Origin origin) {
		ModComponents.ORIGIN.get(player).setOrigin(layer, origin);
		OriginComponent.sync(player);
	}
}
