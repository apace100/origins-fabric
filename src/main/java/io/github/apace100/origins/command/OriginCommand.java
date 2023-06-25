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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OriginCommand {

	private enum TargetType {
		INVOKER,
		SPECIFY
	}

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
					.executes(commandContext -> OriginCommand.openMultipleLayerScreens(commandContext, TargetType.INVOKER))
					.then(argument("targets", EntityArgumentType.players())
						.executes(commandContext -> OriginCommand.openMultipleLayerScreens(commandContext, TargetType.SPECIFY))
						.then(argument("layer", LayerArgumentType.layer())
							.executes(OriginCommand::openSingleLayerScreen)
						)
					)
				)
				.then(literal("random")
					.executes(commandContext -> OriginCommand.randomizeOrigins(commandContext, TargetType.INVOKER))
					.then(argument("targets", EntityArgumentType.players())
						.executes(commandContext -> OriginCommand.randomizeOrigins(commandContext, TargetType.SPECIFY))
						.then(argument("layer", LayerArgumentType.layer())
							.executes(OriginCommand::randomizeOrigin)
						)
					)
				)
		);
	}

	/**
	 * 	Set the origin of the specified entities in the specified origin layer.
	 * 	@param commandContext the command context
	 * 	@return the number of players whose origin has been set
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is <b>not</b> an instance of {@link ServerPlayerEntity}
	 */
	private static int setOrigin(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
		
		Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(commandContext, "targets");
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");
		Origin origin = OriginArgumentType.getOrigin(commandContext, "origin");
		ServerCommandSource serverCommandSource = commandContext.getSource();
		
		int processedTargets = 0;
		
		if (origin.equals(Origin.EMPTY) || originLayer.getOrigins().contains(origin.getIdentifier())) {
			
			for (ServerPlayerEntity target : targets) {
				
				OriginComponent originComponent = ModComponents.ORIGIN.get(target);
				boolean hadOriginBefore = originComponent.hadOriginBefore();
				
				originComponent.setOrigin(originLayer, origin);
				originComponent.sync();
				
				OriginComponent.partialOnChosen(target, hadOriginBefore, origin);
				
				processedTargets++;
				
			}
			
			if (processedTargets == 1) serverCommandSource.sendFeedback(() -> Text.translatable("commands.origin.set.success.single", targets.iterator().next().getDisplayName().getString(), Text.translatable(originLayer.getTranslationKey()), origin.getName()), true);
			else {
				int finalProcessedTargets = processedTargets;
				serverCommandSource.sendFeedback(() -> Text.translatable("commands.origin.set.success.multiple", finalProcessedTargets, Text.translatable(originLayer.getTranslationKey()), origin.getName()), true);
			}
			
		}
		
		else serverCommandSource.sendError(Text.translatable("commands.origin.unregistered_in_layer", origin.getIdentifier(), originLayer.getIdentifier()));
		
		return processedTargets;
		
	}

	/**
	 * 	Check if the specified entities has the specified origin in the specified origin layer.
	 * 	@param commandContext the command context
	 * 	@return the number of players that has the specified origin in the specified origin layer
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is <b>not</b> an instance of {@link ServerPlayerEntity}
	 */
	private static int hasOrigin(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
		
		Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(commandContext, "targets");
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");
		Origin origin = OriginArgumentType.getOrigin(commandContext, "origin");
		ServerCommandSource serverCommandSource = commandContext.getSource();
		
		int processedTargets = 0;
		
		if (origin.equals(Origin.EMPTY) || originLayer.getOrigins().contains(origin.getIdentifier())) {
			
			for (ServerPlayerEntity target : targets) {
				OriginComponent originComponent = ModComponents.ORIGIN.get(target);
				if ((origin.equals(Origin.EMPTY) || originComponent.hasOrigin(originLayer)) && originComponent.getOrigin(originLayer).equals(origin)) processedTargets++;
			}
			
			if (processedTargets == 0) serverCommandSource.sendError(Text.translatable("commands.execute.conditional.fail"));
			else if (processedTargets == 1) serverCommandSource.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), true);
			else {
				int finalProcessedTargets = processedTargets;
				serverCommandSource.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass_count", finalProcessedTargets), true);
			}
			
		}
		
		else serverCommandSource.sendError(Text.translatable("commands.origin.unregistered_in_layer", origin.getIdentifier(), originLayer.getIdentifier()));
		
		return processedTargets;
		
	}

	/**
	 * 	Get the origin of the specified entity from the specified origin layer.
	 * 	@param commandContext the command context
	 * 	@return 1
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is <b>not</b> an instance of {@link ServerPlayerEntity}
	 */
	private static int getOrigin(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {
		
		ServerPlayerEntity target = EntityArgumentType.getPlayer(commandContext, "target");
		ServerCommandSource serverCommandSource = commandContext.getSource();

		OriginComponent originComponent = ModComponents.ORIGIN.get(target);
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");
		Origin origin = originComponent.getOrigin(originLayer);
		
		serverCommandSource.sendFeedback(() -> Text.translatable("commands.origin.get.result", target.getDisplayName().getString(), Text.translatable(originLayer.getTranslationKey()), origin.getName(), origin.getIdentifier()), true);
		
		return 1;
		
	}

	/**
	 * 	Open the 'Choose Origin' screen for the specified origin layer to the specified entities.
	 * 	@param commandContext the command context
	 * 	@return the number of players that had the 'Choose Origin' screen opened for them
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is not an instance of {@link ServerPlayerEntity}
	 */
	private static int openSingleLayerScreen(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = commandContext.getSource();
		Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(commandContext, "targets");
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");

		for (ServerPlayerEntity target : targets) {
			openLayerScreen(target, originLayer);
		}

		serverCommandSource.sendFeedback(() -> Text.translatable("commands.origin.gui.layer", targets.size(), Text.translatable(originLayer.getTranslationKey())), true);
		return targets.size();

	}

	/**
	 * 	Open the 'Choose Origin' screen for all the enabled origin layers to the specified entities.
	 * 	@param commandContext the command context
	 * 	@return the number of players that had the 'Choose Origin' screen opened for them
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is not an instance of {@link ServerPlayerEntity}
	 */
	private static int openMultipleLayerScreens(CommandContext<ServerCommandSource> commandContext, TargetType targetType) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = commandContext.getSource();
		List<ServerPlayerEntity> targets = new ArrayList<>();
		List<OriginLayer> originLayers = OriginLayers.getLayers().stream().toList();

		switch (targetType) {
			case INVOKER -> targets.add(serverCommandSource.getPlayerOrThrow());
			case SPECIFY -> targets.addAll(EntityArgumentType.getPlayers(commandContext, "targets"));
		}

		for (ServerPlayerEntity target : targets) {
			for (OriginLayer originLayer : originLayers) {
				openLayerScreen(target, originLayer);
			}
		}

		serverCommandSource.sendFeedback(() -> Text.translatable("commands.origin.gui.all", targets.size()), false);
		return targets.size();

	}

	/**
	 * 	Randomize the origin of the specified entities in the specified origin layer.
	 * 	@param commandContext the command context
	 * 	@return the number of players that had their origin randomized in the specified origin layer
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is not an instance of {@link ServerPlayerEntity}
	 */
	private static int randomizeOrigin(CommandContext<ServerCommandSource> commandContext) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = commandContext.getSource();
		Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(commandContext, "targets");
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");

		if (originLayer.isRandomAllowed()) {

			Origin origin = null;
			for (ServerPlayerEntity target : targets) {
				origin = getRandomOrigin(target, originLayer);
			}

			if (targets.size() > 1) serverCommandSource.sendFeedback(() -> Text.translatable("commands.origin.random.success.multiple", targets.size(), Text.translatable(originLayer.getTranslationKey())), true);
			else if (targets.size() == 1) {
				Origin finalOrigin = origin;
				serverCommandSource.sendFeedback(() -> Text.translatable("commands.origin.random.success.single", targets.iterator().next().getDisplayName().getString(), finalOrigin.getName(), Text.translatable(originLayer.getTranslationKey())), false);
			}

			return targets.size();

		}

		else {
			serverCommandSource.sendError(Text.translatable("commands.origin.random.not_allowed", Text.translatable(originLayer.getTranslationKey())));
			return 0;
		}

	}

	/**
	 * 	Randomize the origins of the specified entities in all of the origin layers that allows to be randomized.
	 * 	@param commandContext the command context
	 * 	@return the number of players that had their origins randomized in all of the origin layers that allows to be randomized
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is not an instance of {@link ServerPlayerEntity}
	 */
	private static int randomizeOrigins(CommandContext<ServerCommandSource> commandContext, TargetType targetType) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = commandContext.getSource();
		List<ServerPlayerEntity> targets = new ArrayList<>();
		List<OriginLayer> originLayers = OriginLayers.getLayers().stream().filter(OriginLayer::isRandomAllowed).toList();

		switch (targetType) {
			case INVOKER -> targets.add(serverCommandSource.getPlayerOrThrow());
			case SPECIFY -> targets.addAll(EntityArgumentType.getPlayers(commandContext, "targets"));
		}

		for (ServerPlayerEntity target : targets) {
			for (OriginLayer originLayer : originLayers) {
				getRandomOrigin(target, originLayer);
			}
		}

		serverCommandSource.sendFeedback(() -> Text.translatable("commands.origin.random.all", targets.size(), originLayers.size()), false);
		return targets.size();

	}

	private static void openLayerScreen(ServerPlayerEntity target, OriginLayer originLayer) {

		OriginComponent originComponent = ModComponents.ORIGIN.get(target);
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

		if (originLayer.isEnabled()) originComponent.setOrigin(originLayer, Origin.EMPTY);

		originComponent.checkAutoChoosingLayers(target, false);
		originComponent.sync();

		buffer.writeBoolean(false);
		ServerPlayNetworking.send(target, ModPackets.OPEN_ORIGIN_SCREEN, buffer);

	}

	private static Origin getRandomOrigin(ServerPlayerEntity target, OriginLayer originLayer) {

		List<Origin> origins = originLayer.getRandomOrigins(target).stream().map(OriginRegistry::get).toList();
		OriginComponent originComponent = ModComponents.ORIGIN.get(target);
		Origin origin = origins.get(new Random().nextInt(origins.size()));

		boolean hadOriginBefore = originComponent.hadOriginBefore();
		boolean hadAllOrigins = originComponent.hasAllOrigins();

		originComponent.setOrigin(originLayer, origin);
		originComponent.checkAutoChoosingLayers(target, false);
		originComponent.sync();

		if (originComponent.hasAllOrigins() && !hadAllOrigins) OriginComponent.onChosen(target, hadOriginBefore);

		Origins.LOGGER.info(
			"Player {} was randomly assigned the origin {} for layer {}",
			target.getDisplayName().getString(),
			origin.getIdentifier().toString(),
			originLayer.getIdentifier().toString()
		);

		return origin;

	}
	
}
