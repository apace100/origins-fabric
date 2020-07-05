package io.github.apace100.origins.mixin;

import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
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
		if(Origin.get(player) == Origin.EMPTY) {
			PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
			ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, ModPackets.OPEN_ORIGIN_SCREEN, data);
		}
	}
}
