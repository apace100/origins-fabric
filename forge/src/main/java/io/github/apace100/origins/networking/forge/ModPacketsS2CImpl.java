package io.github.apace100.origins.networking.forge;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.registry.ModComponentsArchitectury;
import io.github.apace100.origins.registry.forge.ModComponentsArchitecturyImpl;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.fml.ExtensionPoint;

import java.util.Optional;

public class ModPacketsS2CImpl {

	/**
	 * The content of this function is replaced by {@link ExtensionPoint#DISPLAYTEST}
	 */
	public static void registerPlatformSpecificPackets() {
		NetworkManager.registerReceiver(NetworkManager.s2c(), ModComponentsArchitecturyImpl.SYNC_PACKET_SELF, (packetByteBuf, packetContext) -> packetContext.queue(() -> ModComponentsArchitectury.getOriginComponent(packetContext.getPlayer()).applySyncPacket(packetByteBuf)));
		NetworkManager.registerReceiver(NetworkManager.s2c(), ModComponentsArchitecturyImpl.SYNC_PACKET_OTHER, ModPacketsS2CImpl::receiveOther);
	}

	private static void receiveOther(PacketByteBuf packetByteBuf, NetworkManager.PacketContext packetContext) {
		packetContext.queue(() -> {
			Optional<OriginComponent> component = ModComponentsArchitectury.maybeGetOriginComponent(packetContext.getPlayer().getEntityWorld().getEntityById(packetByteBuf.readVarInt()));
			component.ifPresent(x -> x.applySyncPacket(packetByteBuf));
		});
	}
}
