package io.github.apace100.origins.mixin;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.apace100.origins.access.EndRespawningEntity;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.ModifyPlayerSpawnPower;
import io.github.apace100.origins.power.Power;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PowerTypeRegistry;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("rawtypes")
@Mixin(PlayerManager.class)
public abstract class LoginMixin {

	@Shadow public abstract List<ServerPlayerEntity> getPlayerList();

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V")
	private void openOriginsGui(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
		OriginComponent component = ModComponents.ORIGIN.get(player);

		PacketByteBuf powerListData = new PacketByteBuf(Unpooled.buffer());
		powerListData.writeInt(PowerTypeRegistry.size());
		PowerTypeRegistry.entries().forEach((entry) -> {
			PowerType<?> type = entry.getValue();
			PowerFactory.Instance factory = type.getFactory();
			if(factory != null) {
				powerListData.writeIdentifier(entry.getKey());
				factory.write(powerListData);
				powerListData.writeString(type.getOrCreateNameTranslationKey());
				powerListData.writeString(type.getOrCreateDescriptionTranslationKey());
				powerListData.writeBoolean(type.isHidden());
			}
		});

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

		ServerPlayNetworking.send(player, ModPackets.POWER_LIST, powerListData);
		ServerPlayNetworking.send(player, ModPackets.ORIGIN_LIST, originListData);
		ServerPlayNetworking.send(player, ModPackets.LAYER_LIST, originLayerData);

		List<ServerPlayerEntity> playerList = getPlayerList();
		playerList.forEach(spe -> ModComponents.ORIGIN.syncWith(spe, ComponentProvider.fromEntity(player)));
		OriginComponent.sync(player);
		if(!component.hasAllOrigins()) {
			if(component.checkAutoChoosingLayers(player, true)) {
				component.sync();
			}
			if(component.hasAllOrigins()) {
				component.getOrigins().values().forEach(o -> {
					o.getPowerTypes().forEach(powerType -> component.getPower(powerType).onChosen(false));
				});
			} else {
				PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
				data.writeBoolean(true);
				ServerPlayNetworking.send(player, ModPackets.OPEN_ORIGIN_SCREEN, data);
			}
		}
	}

	@Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/util/math/BlockPos;FZZ)V"))
	private void preventEndExitSpawnPointSetting(ServerPlayerEntity serverPlayerEntity, RegistryKey<World> dimension, BlockPos pos, float angle, boolean spawnPointSet, boolean bl, ServerPlayerEntity playerEntity, boolean alive) {
		EndRespawningEntity ere = (EndRespawningEntity)playerEntity;
		// Prevent setting the spawn point if the player has a "fake" respawn point
		if(ere.hasRealRespawnPoint()) {
			serverPlayerEntity.setSpawnPoint(dimension, pos, angle, spawnPointSet, bl);
		}
	}

	@Inject(method = "remove", at = @At("HEAD"))
	private void invokeOnRemovedCallback(ServerPlayerEntity player, CallbackInfo ci) {
		OriginComponent component = ModComponents.ORIGIN.get(player);
		component.getPowers().forEach(Power::onRemoved);
	}

	@Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;findRespawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;"))
	private Optional<Vec3d> retryObstructedSpawnpointIfFailed(ServerWorld world, BlockPos pos, float f, boolean bl, boolean bl2, ServerPlayerEntity player, boolean alive) {
		Optional<Vec3d> original = PlayerEntity.findRespawnPosition(world, pos, f, bl, bl2);
		if(!original.isPresent()) {
			if(OriginComponent.hasPower(player, ModifyPlayerSpawnPower.class)) {
				return Optional.ofNullable(Dismounting.method_30769(EntityType.PLAYER, world, pos, bl));
			}
		}
		return original;
	}

	@Inject(method = "respawnPlayer", at = @At("HEAD"))
	private void invokePowerRemovedCallback(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
		List<Power> powers = ModComponents.ORIGIN.get(player).getPowers();
		powers.forEach(Power::onRemoved);
	}

	@Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onSpawn()V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void invokePowerRespawnCallback(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir, BlockPos blockPos, float f, boolean bl, ServerWorld serverWorld, Optional optional2, ServerPlayerInteractionManager serverPlayerInteractionManager2, ServerWorld serverWorld2, ServerPlayerEntity serverPlayerEntity) {
		if(!alive) {
			List<Power> powers = ModComponents.ORIGIN.get(serverPlayerEntity).getPowers();
			powers.forEach(Power::onRespawn);
		}
	}
}
