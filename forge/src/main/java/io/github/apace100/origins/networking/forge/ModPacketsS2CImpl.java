package io.github.apace100.origins.networking.forge;

import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.forge.ModComponentsImpl;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraftforge.fml.ExtensionPoint;

import java.util.Objects;

public class ModPacketsS2CImpl {

	/**
	 * The content of this function is replaced by {@link ExtensionPoint#DISPLAYTEST}
	 */
	public static void registerPlatformSpecificPackets() {
		NetworkManager.registerReceiver(NetworkManager.s2c(), ModComponentsImpl.SYNC_PACKET_SELF, (packetByteBuf, packetContext) -> ModComponents.getOriginComponent(packetContext.getPlayer()).applySyncPacket(packetByteBuf));
		NetworkManager.registerReceiver(NetworkManager.s2c(), ModComponentsImpl.SYNC_PACKET_OTHER, (packetByteBuf, packetContext) -> Objects.requireNonNull(ModComponents.getOriginComponent(packetContext.getPlayer().getEntityWorld().getEntityById(packetByteBuf.readVarInt()))).applySyncPacket(packetByteBuf));
	}

}
