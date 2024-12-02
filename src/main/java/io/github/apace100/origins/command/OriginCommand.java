package io.github.apace100.origins.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.command.argument.OriginLayerArgumentType;
import io.github.apace100.origins.command.argument.OriginArgumentType;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.packet.s2c.OpenChooseOriginScreenS2CPacket;
import io.github.apace100.origins.origin.*;
import io.github.apace100.origins.registry.ModComponents;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OriginCommand {

	public static void register(CommandNode<ServerCommandSource> baseNode) {

		//	The main node of the command
		var originNode = literal("origin")
			.requires(source -> source.hasPermissionLevel(2))
			.build();

		//	Add the sub-nodes as children of the main node
		originNode.addChild(SetNode.get());
		originNode.addChild(HasNode.get());
		originNode.addChild(GetNode.get());
		originNode.addChild(GuiNode.get());
		originNode.addChild(RandomNode.get());

		//	Add the main node as a child of the main node
		baseNode.addChild(originNode);

	}

	public static final class SetNode {

		public static CommandNode<ServerCommandSource> get() {
			return literal("set")
				.then(argument("targets", EntityArgumentType.players())
					.then(argument("layer", OriginLayerArgumentType.layer())
						.then(argument("origin", OriginArgumentType.origin())
							.executes(SetNode::execute)))).build();
		}

		public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

			List<ServerPlayerEntity> targets = new ObjectArrayList<>(EntityArgumentType.getPlayers(context, "targets"));

			OriginLayer layer = OriginLayerArgumentType.getLayer(context, "layer");
			Origin origin = OriginArgumentType.getOrigin(context, "origin");

			ServerCommandSource source = context.getSource();
			int processedTargets = 0;

			if (origin.equals(Origin.EMPTY) || layer.getOrigins().contains(origin.getId())) {

				for (ServerPlayerEntity target : targets) {

					OriginComponent originComponent = ModComponents.ORIGIN.get(target);
					boolean hadOriginBefore = originComponent.hadOriginBefore();

					originComponent.setOrigin(layer, origin);
					originComponent.sync();

					OriginComponent.partialOnChosen(target, hadOriginBefore, origin);
					processedTargets++;

				}

				if (processedTargets == 1) {
					source.sendFeedback(() -> Text.translatable("commands.origin.set.success.single", targets.getFirst().getName(), layer.getName(), origin.getName()), true);
				}

				else {
					int finalProcessedTargets = processedTargets;
					source.sendFeedback(() -> Text.translatable("commands.origin.set.success.multiple", finalProcessedTargets, layer.getName(), origin.getName()), true);
				}

			}

			else {
				source.sendError(Text.stringifiedTranslatable("commands.origin.unregistered_in_layer", origin.getId(), layer.getId()));
			}

			return processedTargets;

		}

	}

	public static final class HasNode {

		public static CommandNode<ServerCommandSource> get() {
			return literal("has")
				.then(argument("targets", EntityArgumentType.players())
					.then(argument("layer", OriginLayerArgumentType.layer())
						.then(argument("origin", OriginArgumentType.origin())
							.executes(HasNode::execute)))).build();
		}

		public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

			List<ServerPlayerEntity> targets = new ObjectArrayList<>(EntityArgumentType.getPlayers(context, "targets"));

			OriginLayer layer = OriginLayerArgumentType.getLayer(context, "layer");
			Origin origin = OriginArgumentType.getOrigin(context, "origin");

			ServerCommandSource source = context.getSource();
			int processedTargets = 0;

			if (origin.equals(Origin.EMPTY) || layer.getOrigins().contains(origin.getId())) {

				for (ServerPlayerEntity target : targets) {

					OriginComponent origincomponent = ModComponents.ORIGIN.get(target);

					if ((origin.equals(Origin.EMPTY) || origincomponent.hasOrigin(layer)) && origincomponent.getOrigin(layer).equals(origin)) {
						processedTargets++;
					}

				}

				if (processedTargets == 0) {
					source.sendError(Text.translatable("commands.execute.conditional.fail"));
				}

				else if (processedTargets == 1) {
					source.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), false);
				}

				else {
					int finalProcessedTargets = processedTargets;
					source.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass_count", finalProcessedTargets), false);
				}

			}

			else {
				source.sendError(Text.stringifiedTranslatable("commands.origin.unregistered_in_layer", origin.getId(), layer.getId()));
			}

			return processedTargets;

		}

	}

	public static final class GetNode {

		public static CommandNode<ServerCommandSource> get() {
			return literal("get")
				.then(argument("target", EntityArgumentType.player())
					.then(argument("layer", OriginLayerArgumentType.layer())
						.executes(GetNode::execute))).build();
		}

		public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

			ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
			OriginLayer layer = OriginLayerArgumentType.getLayer(context, "layer");

			ServerCommandSource source = context.getSource();
			OriginComponent originComponent = ModComponents.ORIGIN.get(target);

			Origin origin = originComponent.getOrigin(layer);
			source.sendFeedback(() -> Text.translatable("commands.origin.get.result", target.getName(), layer.getName(), origin.getName(), origin.getId().toString()), false);

			return 1;

		}

	}

	public static final class GuiNode {

		public static CommandNode<ServerCommandSource> get() {
			return literal("gui")
				.executes(context -> openAll(context, true))
				.then(argument("targets", EntityArgumentType.players())
					.executes(context -> openAll(context, false))
					.then(argument("layer", OriginLayerArgumentType.layer())
						.executes(GuiNode::openSpecific))).build();
		}

		public static int openAll(CommandContext<ServerCommandSource> context, boolean self) throws CommandSyntaxException {

			ServerCommandSource source = context.getSource();
			Collection<ServerPlayerEntity> targets = self
				? List.of(source.getPlayerOrThrow())
				: EntityArgumentType.getPlayers(context, "targets");

			for (ServerPlayerEntity target : targets) {
				openLayer(target, Optional.empty());
			}

			source.sendFeedback(() -> Text.translatable("commands.origin.gui.all", targets.size()), true);
			return targets.size();

		}

		public static int openSpecific(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

			Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
			OriginLayer layer = OriginLayerArgumentType.getLayer(context, "layer");

			for (ServerPlayerEntity target : targets) {
				openLayer(target, Optional.of(layer));
			}

			context.getSource().sendFeedback(() -> Text.translatable("commands.origin.gui.layer", targets.size(), layer.getName()), true);
			return targets.size();

		}

	}

	public static final class RandomNode {

		public static CommandNode<ServerCommandSource> get() {
			return literal("random")
				.executes(context -> randomizeAll(context, true))
				.then(argument("targets", EntityArgumentType.players())
					.executes(context -> randomizeAll(context, false))
					.then(argument("layer", OriginLayerArgumentType.layer())
						.executes(RandomNode::randomizeSpecific))).build();
		}

		public static int randomizeAll(CommandContext<ServerCommandSource> context, boolean self) throws CommandSyntaxException {

			ServerCommandSource source = context.getSource();
			Collection<ServerPlayerEntity> targets = self
				? List.of(source.getPlayerOrThrow())
				: EntityArgumentType.getPlayers(context, "targets");

			List<OriginLayer> layers = OriginLayerManager.values()
				.stream()
				.filter(OriginLayer::isRandomAllowed)
				.toList();

			for (ServerPlayerEntity target : targets) {

				for (OriginLayer layer : layers) {
					setAndGetRandomOrigin(target, layer);
				}

			}

			source.sendFeedback(() -> Text.translatable("commands.origin.random.all", targets.size(), layers.size()), true);
			return targets.size();

		}

		public static int randomizeSpecific(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

			List<ServerPlayerEntity> targets = new ObjectArrayList<>(EntityArgumentType.getPlayers(context, "targets"));
			OriginLayer layer = OriginLayerArgumentType.getLayer(context, "layer");

			ServerCommandSource source = context.getSource();
			AtomicReference<Origin> origin = new AtomicReference<>();

			if (layer.isRandomAllowed()) {

				for (ServerPlayerEntity target : targets) {
					origin.set(setAndGetRandomOrigin(target, layer));
				}

				if (targets.size() > 1) {
					source.sendFeedback(() -> Text.translatable("commands.origin.random.success.multiple", targets.size(), layer.getName()), true);
				}

				else {
					source.sendFeedback(() -> Text.translatable("commands.origin.random.success.single", targets.getFirst().getName(), origin.get().getName(), layer.getName()), true);
				}

			}

			return targets.size();

		}

	}

	private static Origin setAndGetRandomOrigin(ServerPlayerEntity target, OriginLayer layer) {

		List<Origin> origins = layer.getRandomOrigins(target)
			.stream()
			.filter(OriginManager::contains)
			.map(OriginManager::get)
			.toList();

		OriginComponent originComponent = ModComponents.ORIGIN.get(target);
		Origin origin = origins.get(Random.create().nextInt(origins.size()));

		boolean hadOriginBefore = originComponent.hadOriginBefore();
		boolean hadAllOrigins = originComponent.hasAllOrigins();

		originComponent.setOrigin(layer, origin);
		originComponent.checkAutoChoosingLayers(target, false);

		originComponent.sync();

		if (originComponent.hasAllOrigins() && !hadAllOrigins) {
			OriginComponent.onChosen(target, hadOriginBefore);
		}

		Origins.LOGGER.info("Player {} was randomly assigned the origin {} for layer {}", target.getName().getString(), origin.getId(), layer.getId());
		return origin;

	}

	private static void openLayer(ServerPlayerEntity target, Optional<OriginLayer> targetLayer) {

		OriginComponent originComponent = ModComponents.ORIGIN.get(target);
		List<OriginLayer> layers = new ObjectArrayList<>();

		targetLayer.ifPresentOrElse(layers::add, () -> layers.addAll(OriginLayerManager.values()));

		layers.stream()
			.filter(OriginLayer::isEnabled)
			.forEach(layer -> originComponent.setOrigin(layer, Origin.EMPTY));

		boolean automaticallyAssigned = originComponent.checkAutoChoosingLayers(target, false);
		int options = targetLayer
			.map(layer -> layer.getOriginOptionCount(target))
			.orElseGet(() -> OriginLayerManager.getOriginOptionCount(target));

		originComponent.selectingOrigin(!automaticallyAssigned || options > 0);
		originComponent.sync();

		if (originComponent.isSelectingOrigin()) {
			ServerPlayNetworking.send(target, new OpenChooseOriginScreenS2CPacket(false));
		}

	}

}
