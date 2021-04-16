package io.github.apace100.origins.networking.forge;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.forge.ModComponentsImpl;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.fml.ExtensionPoint;

import java.util.Objects;
import java.util.Optional;

public class ModPacketsS2CImpl {

	/**
	 * The content of this function is replaced by {@link ExtensionPoint#DISPLAYTEST}
	 */
	public static void registerPlatformSpecificPackets() {
		NetworkManager.registerReceiver(NetworkManager.s2c(), ModComponentsImpl.SYNC_PACKET_SELF, (packetByteBuf, packetContext) -> packetContext.queue(() -> ModComponents.getOriginComponent(packetContext.getPlayer()).applySyncPacket(packetByteBuf)));
		NetworkManager.registerReceiver(NetworkManager.s2c(), ModComponentsImpl.SYNC_PACKET_OTHER, ModPacketsS2CImpl::receiveOther);
	}

	private static void receiveOther(PacketByteBuf packetByteBuf, NetworkManager.PacketContext packetContext) {
		packetContext.queue(() -> {
			Optional<OriginComponent> component = ModComponents.maybeGetOriginComponent(packetContext.getPlayer().getEntityWorld().getEntityById(packetByteBuf.readVarInt()));
			component.ifPresent(x -> x.applySyncPacket(packetByteBuf));
		});
	}
}
