package io.github.apace100.origins;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PowerTypeRegistry;
import io.github.apace100.origins.power.PreventItemUsePower;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import me.shedaniel.architectury.event.events.InteractionEvent;
import me.shedaniel.architectury.event.events.PlayerEvent;
import me.shedaniel.architectury.event.events.TickEvent;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class OriginEventHandler {
	public static void register() {
		//Replaces BlockItemMixin
		InteractionEvent.RIGHT_CLICK_BLOCK.register(OriginEventHandler::preventItemUse);
		//Replaces ItemStackMixin
		InteractionEvent.RIGHT_CLICK_ITEM.register(OriginEventHandler::preventItemUse);
		//Replaces LoginMixin#openOriginsGui
		PlayerEvent.PLAYER_JOIN.register(OriginEventHandler::playerJoin);
		//Replaces LoginMixin#invokePowerRespawnCallback
		PlayerEvent.PLAYER_RESPAWN.register(OriginEventHandler::respawn);
	}

	private static ActionResult preventItemUse(PlayerEntity user, Hand hand, BlockPos pos, Direction face) {
		if (user != null) {
			OriginComponent component = ModComponents.getOriginComponent(user);
			ItemStack stackInHand = user.getStackInHand(hand);
			for (PreventItemUsePower piup : component.getPowers(PreventItemUsePower.class)) {
				if (piup.doesPrevent(stackInHand)) {
					return ActionResult.FAIL;
				}
			}
		}
		return ActionResult.PASS;
	}

	private static TypedActionResult<ItemStack> preventItemUse(PlayerEntity user, Hand hand) {
		if (user != null) {
			OriginComponent component = ModComponents.getOriginComponent(user);
			ItemStack stackInHand = user.getStackInHand(hand);
			for (PreventItemUsePower piup : component.getPowers(PreventItemUsePower.class)) {
				if (piup.doesPrevent(stackInHand)) {
					return TypedActionResult.fail(stackInHand);
				}
			}
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
		return TypedActionResult.pass(ItemStack.EMPTY);
	}

	/**
	 * Replaces {@code LoginMixin.openOriginsGui(ClientConnection, ServerPlayerEntity, CallbackInfo)}
	 */
	private static void playerJoin(ServerPlayerEntity player) {
		OriginComponent component = ModComponents.getOriginComponent(player);

		PacketByteBuf powerListData = new PacketByteBuf(Unpooled.buffer());
		powerListData.writeInt(PowerTypeRegistry.size());
		PowerTypeRegistry.entries().forEach((entry) -> {
			PowerType<?> type = entry.getValue();
			PowerFactory<?>.Instance factory = type.getFactory();
			if (factory != null) {
				powerListData.writeIdentifier(entry.getKey());
				factory.write(powerListData);
				powerListData.writeString(type.getOrCreateNameTranslationKey());
				powerListData.writeString(type.getOrCreateDescriptionTranslationKey());
				powerListData.writeBoolean(type.isHidden());
			}
		});

		PacketByteBuf originListData = new PacketByteBuf(Unpooled.buffer());
		originListData.writeInt(OriginRegistry.size() - 1);
		OriginRegistry.entries().forEach((entry) -> {
			if (entry.getValue() != Origin.EMPTY) {
				originListData.writeIdentifier(entry.getKey());
				entry.getValue().write(originListData);
			}
		});

		PacketByteBuf originLayerData = new PacketByteBuf(Unpooled.buffer());
		originLayerData.writeInt(OriginLayers.size());
		OriginLayers.getLayers().forEach((layer) -> {
			layer.write(originLayerData);
			if (layer.isEnabled()) {
				if (!component.hasOrigin(layer)) {
					component.setOrigin(layer, Origin.EMPTY);
				}
			}
		});

		NetworkManager.sendToPlayer(player, ModPackets.POWER_LIST, powerListData);
		NetworkManager.sendToPlayer(player, ModPackets.ORIGIN_LIST, originListData);
		NetworkManager.sendToPlayer(player, ModPackets.LAYER_LIST, originLayerData);

		List<ServerPlayerEntity> playerList = player.getServer().getPlayerManager().getPlayerList();
		playerList.forEach(spe -> ModComponents.syncWith(spe, player));
		OriginComponent.sync(player);
		if (!component.hasAllOrigins()) {
			PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
			data.writeBoolean(true);
			NetworkManager.sendToPlayer(player, ModPackets.OPEN_ORIGIN_SCREEN, data);
		}
	}

	private static void respawn(ServerPlayerEntity serverPlayerEntity, boolean alive) {
		if (!alive) {
			List<Power> powers = ModComponents.getOriginComponent(serverPlayerEntity).getPowers();
			powers.forEach(Power::onRespawn);
		}
	}
}
