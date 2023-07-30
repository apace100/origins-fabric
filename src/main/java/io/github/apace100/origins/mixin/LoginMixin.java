package io.github.apace100.origins.mixin;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

@SuppressWarnings("rawtypes")
@Mixin(PlayerManager.class)
public abstract class LoginMixin {

	@Shadow public abstract List<ServerPlayerEntity> getPlayerList();

	@Inject(at = @At("TAIL"), method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V")
	private void openOriginsGui(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
		OriginComponent component = ModComponents.ORIGIN.get(player);

		PacketByteBuf originListData = new PacketByteBuf(Unpooled.buffer());
		originListData.writeInt(OriginRegistry.size() - 1);
		OriginRegistry.entries().forEach((entry) -> {
			if(entry.getValue() != Origin.EMPTY) {
				originListData.writeIdentifier(entry.getKey());
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

		ServerPlayNetworking.send(player, ModPackets.ORIGIN_LIST, originListData);
		ServerPlayNetworking.send(player, ModPackets.LAYER_LIST, originLayerData);

		BadgeManager.sync(player);

		List<ServerPlayerEntity> playerList = getPlayerList();
		playerList.forEach(spe -> ModComponents.ORIGIN.syncWith(spe, (ComponentProvider)player));
		OriginComponent.sync(player);
		if(!component.hasAllOrigins()) {
			if(component.checkAutoChoosingLayers(player, true)) {
				component.sync();
			}
			if(component.hasAllOrigins()) {
				OriginComponent.onChosen(player, false);
			} else {
				PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
				data.writeBoolean(true);
				ServerPlayNetworking.send(player, ModPackets.OPEN_ORIGIN_SCREEN, data);
			}
		}
	}
}
