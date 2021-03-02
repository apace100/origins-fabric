package io.github.apace100.origins.networking.fabric;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.networking.ModPackets;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModPacketsS2CImpl {

	public static void registerPlatformSpecificPackets() {
		ClientLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsS2CImpl::handleHandshake);
	}

	@Environment(EnvType.CLIENT)
	private static CompletableFuture<PacketByteBuf> handleHandshake(MinecraftClient minecraftClient, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf packetByteBuf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(Origins.SEMVER.length);
		for(int i = 0; i < Origins.SEMVER.length; i++) {
			buf.writeInt(Origins.SEMVER[i]);
		}
		OriginsClient.isServerRunningOrigins = true;
		return CompletableFuture.completedFuture(buf);
	}
}
