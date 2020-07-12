package io.github.apace100.origins.mixin;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class LoginMixin {

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V")
	private void openOriginsGui(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
		PacketByteBuf originListData = new PacketByteBuf(Unpooled.buffer());
		originListData.writeInt(OriginRegistry.size() - 2);
		OriginRegistry.entries().forEach((entry) -> {
			if(!(entry.getValue() == Origin.EMPTY || entry.getValue() == Origin.HUMAN)) {
				originListData.writeString(entry.getKey().toString());
				entry.getValue().write(originListData);
			}
		});
		ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ModPackets.ORIGIN_LIST, originListData);
		if(Origin.get(player) == Origin.EMPTY) {
			PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
			data.writeBoolean(true);
			ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ModPackets.OPEN_ORIGIN_SCREEN, data);
		} else {
			OriginComponent.sync(player);
		}
	}
}
