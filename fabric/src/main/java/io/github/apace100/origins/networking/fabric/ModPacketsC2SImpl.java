package io.github.apace100.origins.networking.fabric;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.networking.ModPackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class ModPacketsC2SImpl {

	public static void registerPlatformSpecificPackets() {
		ServerLoginConnectionEvents.QUERY_START.register(ModPacketsC2SImpl::handshake);
		ServerLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsC2SImpl::handleHandshakeReply);
	}

	private static void handshake(ServerLoginNetworkHandler serverLoginNetworkHandler, MinecraftServer minecraftServer, PacketSender packetSender, ServerLoginNetworking.LoginSynchronizer loginSynchronizer) {
		packetSender.sendPacket(ModPackets.HANDSHAKE, new PacketByteBuf(Unpooled.EMPTY_BUFFER));
	}

	private static void handleHandshakeReply(MinecraftServer minecraftServer, ServerLoginNetworkHandler serverLoginNetworkHandler, boolean understood, PacketByteBuf packetByteBuf, ServerLoginNetworking.LoginSynchronizer loginSynchronizer, PacketSender packetSender) {
		if (understood) {
			int clientSemVerLength = packetByteBuf.readInt();
			int[] clientSemVer = new int[clientSemVerLength];
			boolean mismatch = clientSemVerLength != Origins.SEMVER.length;
			for(int i = 0; i < clientSemVerLength; i++) {
				clientSemVer[i] = packetByteBuf.readInt();
				if(i < clientSemVerLength - 1 && clientSemVer[i] != Origins.SEMVER[i]) {
					mismatch = true;
				}
			}
			if(mismatch) {
				StringBuilder clientVersionString = new StringBuilder();
				for(int i = 0; i < clientSemVerLength; i++) {
					clientVersionString.append(clientSemVer[i]);
					if(i < clientSemVerLength - 1) {
						clientVersionString.append(".");
					}
				}
				serverLoginNetworkHandler.disconnect(new TranslatableText("origins.gui.version_mismatch", Origins.VERSION, clientVersionString));
			}
		} else {
			serverLoginNetworkHandler.disconnect(new LiteralText("This server requires you to install the Origins mod (v" + Origins.VERSION + ") to play."));
		}
	}
}
