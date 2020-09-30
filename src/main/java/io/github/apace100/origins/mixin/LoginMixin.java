package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.PowerTypeRegistry;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerManager.class)
public abstract class LoginMixin {

	@Shadow public abstract List<ServerPlayerEntity> getPlayerList();

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V")
	private void openOriginsGui(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
		OriginComponent component = ModComponents.ORIGIN.get(player);

		PacketByteBuf powerListData = new PacketByteBuf(Unpooled.buffer());
		powerListData.writeInt(PowerTypeRegistry.size());
		PowerTypeRegistry.entries().forEach((entry) -> {
			PowerFactory factory = entry.getValue().getFactory();
			if(factory != null) {
				powerListData.writeIdentifier(entry.getKey());
				PowerFactory.write(factory, powerListData);
			}
		});

		PacketByteBuf originListData = new PacketByteBuf(Unpooled.buffer());
		originListData.writeInt(OriginRegistry.size() - 1);
		OriginRegistry.entries().forEach((entry) -> {
			if(entry.getValue() != Origin.EMPTY) {
				originListData.writeString(entry.getKey().toString());
				entry.getValue().write(originListData);
			}
		});

		PacketByteBuf originLayerData = new PacketByteBuf(Unpooled.buffer());
		originLayerData.writeInt(OriginLayers.size());
		OriginLayers.getLayers().forEach((layer) -> {
			layer.write(originLayerData);
			if(layer.isEnabled()) {
				if(!component.hasOrigin(layer)) {
					component.setOrigin(layer, Origin.EMPTY);
				}
			}
		});

		ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ModPackets.POWER_LIST, powerListData);
		ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ModPackets.ORIGIN_LIST, originListData);
		ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ModPackets.LAYER_LIST, originLayerData);

		List<ServerPlayerEntity> playerList = getPlayerList();
		playerList.forEach(spe -> ModComponents.ORIGIN.get(spe).syncWith(player));
		OriginComponent.sync(player);
		if(!component.hasAllOrigins()) {
			PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
			data.writeBoolean(true);
			ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ModPackets.OPEN_ORIGIN_SCREEN, data);
		}
	}
}
