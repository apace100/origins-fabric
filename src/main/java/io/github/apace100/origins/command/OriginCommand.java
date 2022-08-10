package io.github.apace100.origins.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Random;

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
								.executes(OriginCommand::setOrigin))))
				)
				.then(literal("has")
					.then(argument("targets", EntityArgumentType.players())
						.then(argument("layer", LayerArgumentType.layer())
							.then(argument("origin", OriginArgumentType.origin())
								.executes(OriginCommand::hasOrigin))))
				)
				.then(literal("get")
					.then(argument("target", EntityArgumentType.player())
						.then(argument("layer", LayerArgumentType.layer())
							.executes(OriginCommand::getOrigin)
						)
					)
				)
				.then(literal("gui")
					.then(argument("targets", EntityArgumentType.players())
						.executes(commandContext -> OriginCommand.openGui(commandContext, false))
						.then(argument("layer", LayerArgumentType.layer())
							.executes(commandContext -> OriginCommand.openGui(commandContext, true))
						)
					)
				)
				.then(literal("random")
					.then(argument("targets", EntityArgumentType.players())
						.then(argument("layer", LayerArgumentType.layer())
							.executes(OriginCommand::randomizeOrigin)
						)
					)
				)
		);
	}
	
	/*
		Sets the origin of the player in the specified origin layer
	 */
	private static int setOrigin(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
		
		Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(commandContext, "targets");
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");
		Origin origin = OriginArgumentType.getOrigin(commandContext, "origin");
		ServerCommandSource serverCommandSource = commandContext.getSource();
		
		int processedTargets = 0;
		
		if (originLayer.getOrigins().contains(origin.getIdentifier())) {
			
			for (ServerPlayerEntity target : targets) {
				
				OriginComponent originComponent = ModComponents.ORIGIN.get(target);
				boolean hadOriginBefore = originComponent.hadOriginBefore();
				
				originComponent.setOrigin(originLayer, origin);
				originComponent.sync();
				
				OriginComponent.partialOnChosen(target, hadOriginBefore, origin);
				
				processedTargets++;
				
			}
			
			if (processedTargets == 1) serverCommandSource.sendFeedback(Text.translatable("commands.origin.set.success.single", targets.iterator().next().getDisplayName().getString(), Text.translatable(originLayer.getTranslationKey()), origin.getName()), true);
			else serverCommandSource.sendFeedback(Text.translatable("commands.origin.set.success.multiple", processedTargets, Text.translatable(originLayer.getTranslationKey()), origin.getName()), true);
			
		}
		
		else serverCommandSource.sendError(Text.translatable("commands.origin.origin_in_layer_not_found", origin.getIdentifier(), originLayer.getIdentifier()));
		
		return processedTargets;
		
	}
	
	/*
		Returns the number of players that has the specified origin in the specified origin layer.
		(Useful for checking if a player has a certain origin in functions.)
	 */
	private static int hasOrigin(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
		
		Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(commandContext, "targets");
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");
		Origin origin = OriginArgumentType.getOrigin(commandContext, "origin");
		ServerCommandSource serverCommandSource = commandContext.getSource();
		
		int processedTargets = 0;
		
		if (originLayer.getOrigins().contains(origin.getIdentifier())) {
			
			for (ServerPlayerEntity target : targets) {
				OriginComponent originComponent = ModComponents.ORIGIN.get(target);
				if (originComponent.hasOrigin(originLayer) && originComponent.getOrigin(originLayer).equals(origin)) processedTargets++;
			}
			
			if (processedTargets == 0) serverCommandSource.sendError(Text.translatable("commands.execute.conditional.fail"));
			else if (processedTargets == 1) serverCommandSource.sendFeedback(Text.translatable("commands.execute.conditional.pass"), true);
			else serverCommandSource.sendFeedback(Text.translatable("commands.execute.conditional.pass_count", processedTargets), true);
			
		}
		
		else serverCommandSource.sendError(Text.translatable("commands.origin.origin_in_layer_not_found", origin.getIdentifier(), originLayer.getIdentifier()));
		
		return processedTargets;
		
	}
	
	/*
		Gets the origin of the player from the specified origin layer
	 */
	private static int getOrigin(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
		
		ServerPlayerEntity target = EntityArgumentType.getPlayer(commandContext, "target");
		ServerCommandSource serverCommandSource = commandContext.getSource();
		OriginComponent originComponent = ModComponents.ORIGIN.get(target);
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");
		Origin origin = originComponent.getOrigin(originLayer);
		
		serverCommandSource.sendFeedback(Text.translatable("commands.origin.get.result", target.getDisplayName().getString(), Text.translatable(originLayer.getTranslationKey()), origin.getName(), origin.getIdentifier()), true);
		
		return 1;
		
	}
	
	/*
		Opens the 'Choose Origin' screen for the specified origin layer *(or all enabled origin layers, if not specified).*
	 */
	private static int openGui(CommandContext<ServerCommandSource> commandcontext, boolean hasOriginLayer) throws CommandSyntaxException {
		
		Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(commandcontext, "targets");
		ServerCommandSource serverCommandSource = commandcontext.getSource();
		OriginLayer originLayer = null;
		if (hasOriginLayer) originLayer = LayerArgumentType.getLayer(commandcontext, "layer");

		int processedTargets = 0;
		
		for (ServerPlayerEntity target : targets) {
			
			OriginComponent originComponent = ModComponents.ORIGIN.get(target);
			PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
			
			if (hasOriginLayer && originLayer.isEnabled()) originComponent.setOrigin(originLayer, Origin.EMPTY);
			else OriginLayers.getLayers().forEach(
				layer -> {
					if (layer.isEnabled()) originComponent.setOrigin(layer, Origin.EMPTY);
				}
			);
			
			originComponent.checkAutoChoosingLayers(target, false);
			originComponent.sync();
			
			buffer.writeBoolean(false);
			ServerPlayNetworking.send(target, ModPackets.OPEN_ORIGIN_SCREEN, buffer);
			
			processedTargets++;
			
		}
		
		serverCommandSource.sendFeedback(Text.translatable("commands.origin.gui.all", processedTargets), false);
		return processedTargets;
		
	}
	
	/*
		Set the origin of the player in the specified origin layer randomly.
	 */
	private static int randomizeOrigin(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
		
		Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(commandContext, "targets");
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");
		ServerCommandSource serverCommandSource = commandContext.getSource();

		Entity sourceEntity = serverCommandSource.getEntity();
		Entity targetEntity = targets.iterator().next();
		Origin origin = null;
		
		int processedTargets = 0;
		
		if (originLayer.isRandomAllowed()) {
			
			for (ServerPlayerEntity target : targets) {
				
				OriginComponent originComponent = ModComponents.ORIGIN.get(target);
				List<Origin> randomOrigins = originLayer.getRandomOrigins(target).stream().map(OriginRegistry::get).toList();
				origin = randomOrigins.get(new Random().nextInt(randomOrigins.size()));
				
				boolean hadOriginBefore = originComponent.hadOriginBefore();
				boolean hadAllOrigins = originComponent.hasAllOrigins();
				
				originComponent.setOrigin(originLayer, origin);
				originComponent.checkAutoChoosingLayers(target, false);
				originComponent.sync();
				
				if (originComponent.hasAllOrigins() && !hadAllOrigins) OriginComponent.onChosen(target ,hadOriginBefore);

				Origins.LOGGER.info(
					"Player {} was randomly assigned the following Origin: {} for layer: {}",
					target.getDisplayName().getString(),
					origin.getIdentifier().toString(),
					originLayer.getIdentifier().toString()
				);
				
				target.sendMessage(
					Text.translatable(
						"commands.origin.random.success.to_target",
						origin.getName(),
						Text.translatable(originLayer.getTranslationKey())
					),
					false
				);
				
				processedTargets++;
				
			}
			
			if (processedTargets == 1) {
				if (sourceEntity != null && !sourceEntity.equals(targetEntity)) serverCommandSource.sendFeedback(Text.translatable("commands.origin.random.success.single", targetEntity.getDisplayName().getString(), origin.getName(), Text.translatable(originLayer.getTranslationKey())), true);
			}
			else serverCommandSource.sendFeedback(Text.translatable("commands.origin.random.success.multiple", processedTargets, Text.translatable(originLayer.getTranslationKey())), true);
			
		}
		
		else {
			if (targets.size() == 1) serverCommandSource.sendFeedback(Text.translatable("commands.origin.random.fail.single", targetEntity.getDisplayName().getString(), Text.translatable(originLayer.getTranslationKey()), Text.translatable("commands.origin.layer_random_not_allowed")), true);
			else serverCommandSource.sendFeedback(Text.translatable("commands.origin.random.fail.multiple", targets.size(), Text.translatable(originLayer.getTranslationKey()), Text.translatable("commands.origin.layer_random_not_allowed")), true);
		}
		
		return processedTargets;
		
	}
	
}
