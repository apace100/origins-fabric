package io.github.apace100.origins.registry.forge;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.components.DummyOriginComponent;
import io.netty.buffer.Unpooled;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.forge.NetworkManagerImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Optional;

public class ModComponentsArchitecturyImpl {
	public static final Identifier SYNC_PACKET_SELF = Origins.identifier("forge/sync_origin_self");
	public static final Identifier SYNC_PACKET_OTHER = Origins.identifier("forge/sync_origin_other");


	@CapabilityInject(OriginComponent.class)
	public static Capability<OriginComponent> ORIGIN_COMPONENT_CAPABILITY;

	public static OriginComponent getOriginComponent(Entity player) {
		if (player instanceof PlayerEntity)
			return player.getCapability(ORIGIN_COMPONENT_CAPABILITY).orElseGet(DummyOriginComponent::getInstance);
		return DummyOriginComponent.getInstance();
	}

	public static void syncOriginComponent(Entity player) {
		Packet<?> packet = buildOtherPacket(player);
		if (packet != null)
			PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player).send(packet);
	}

	public static Packet<?> buildOtherPacket(Entity entity) {
		Optional<OriginComponent> originComponent = maybeGetOriginComponent(entity);
		if (originComponent.isPresent()) {
			PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
			CompoundTag tag = new CompoundTag();
			originComponent.get().writeToNbt(tag);
			buffer.writeVarInt(entity.getEntityId());
			buffer.writeCompoundTag(tag);
			return NetworkManagerImpl.toPacket(NetworkManager.Side.S2C, SYNC_PACKET_OTHER, buffer);
		}
		return null;
	}

	public static void syncWith(ServerPlayerEntity player, Entity provider) {
		Packet<?> packet = buildOtherPacket(provider);
		if (packet != null)
			PacketDistributor.PLAYER.with(() -> player).send(packet);
	}

	public static Optional<OriginComponent> maybeGetOriginComponent(Entity player) {
		if (player instanceof PlayerEntity)
			return player.getCapability(ORIGIN_COMPONENT_CAPABILITY).resolve();
		return Optional.empty();
	}

	public static class OriginStorage implements Capability.IStorage<OriginComponent> {
		@Override
		public Tag writeNBT(Capability < OriginComponent > capability, OriginComponent object, Direction arg) {
			CompoundTag tag = new CompoundTag();
			object.writeToNbt(tag);
			return tag;
		}

		@Override
		public void readNBT(Capability<OriginComponent> capability, OriginComponent object, Direction arg, Tag arg2) {
			object.readFromNbt((CompoundTag) arg2);
		}
	}
}
