package io.github.apace100.origins.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
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
						.then(argument("layer", LayerArgumentType.layer())
							.then(argument("origin", OriginArgumentType.origin())
								.executes((command) -> {
									// Sets the origins of several people in the given layer.
									int i = 0;
									Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(command, "targets");
									OriginLayer l = LayerArgumentType.getLayer(command, "layer");
									Origin o = OriginArgumentType.getOrigin(command, "origin");
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
					.then(argument("targets", EntityArgumentType.players())
						.then(argument("layer", LayerArgumentType.layer())
							.then(argument("origin", OriginArgumentType.origin())
								.executes((command) -> {
									// Returns the number of people in the target selector with the origin in the given layer.
									// Useful for checking if a player has the given origin in functions.
									int i = 0;
									Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(command, "targets");
									OriginLayer l = LayerArgumentType.getLayer(command, "layer");
									Origin o = OriginArgumentType.getOrigin(command, "origin");
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
				.then(literal("get")
					.then(argument("target", EntityArgumentType.player())
						.then(argument("layer", LayerArgumentType.layer())
							.executes((command) -> {
								ServerPlayerEntity target = EntityArgumentType.getPlayer(command, "target");
								OriginLayer layer = LayerArgumentType.getLayer(command, "layer");
								OriginComponent component = ModComponents.ORIGIN.get(target);
								Origin origin = component.getOrigin(layer);
								command.getSource().sendFeedback(new TranslatableText("commands.origin.get.result", target.getDisplayName(), new TranslatableText(layer.getTranslationKey()), origin.getName(), origin.getIdentifier()), false);
								return 1;
							})
						)
					)
				)
				.then(literal("gui")
					.then(argument("targets", EntityArgumentType.players())
						.executes((command) -> {
							Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(command, "targets");
							targets.forEach(target -> {
								OriginComponent component = ModComponents.ORIGIN.get(target);
								OriginLayers.getLayers().forEach(layer -> {
									if(layer.isEnabled()) {
										component.setOrigin(layer, Origin.EMPTY);
									}
								});
								component.checkAutoChoosingLayers(target, false);
								component.sync();
								PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
								data.writeBoolean(false);
								ServerPlayNetworking.send(target, ModPackets.OPEN_ORIGIN_SCREEN, data);
							});
							command.getSource().sendFeedback(new TranslatableText("commands.origin.gui.all", targets.size()), false);
							return targets.size();
						})
						.then(argument("layer", LayerArgumentType.layer())
							.executes((command) -> {
								OriginLayer layer = LayerArgumentType.getLayer(command, "layer");
								Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(command, "targets");
								targets.forEach(target -> {
									OriginComponent component = ModComponents.ORIGIN.get(target);
									if(layer.isEnabled()) {
										component.setOrigin(layer, Origin.EMPTY);
									}
									component.checkAutoChoosingLayers(target, false);
									component.sync();
									PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
									data.writeBoolean(false);
									ServerPlayNetworking.send(target, ModPackets.OPEN_ORIGIN_SCREEN, data);
								});
								command.getSource().sendFeedback(new TranslatableText("commands.origin.gui.layer", targets.size(), new TranslatableText(layer.getTranslationKey())), false);
								return targets.size();
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
		OriginComponent.partialOnChosen(player, hadOriginBefore, origin);
	}

	private static boolean hasOrigin(PlayerEntity player, OriginLayer layer, Origin origin) {
		OriginComponent component = ModComponents.ORIGIN.get(player);
		return component.hasOrigin(layer) && component.getOrigin(layer).equals(origin);
	}
}
