package io.github.apace100.origins.mixin;

import io.github.apace100.origins.access.EndRespawningEntity;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onClientStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;respawnPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;Z)Lnet/minecraft/server/network/ServerPlayerEntity;", ordinal = 0))
    private void saveEndRespawnStatus(ClientStatusC2SPacket packet, CallbackInfo ci) {
        ((EndRespawningEntity)this.player).setEndRespawning(true);
    }

    @Inject(method = "onClientStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/ChangedDimensionCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/util/registry/RegistryKey;)V"))
    private void undoEndRespawnStatus(ClientStatusC2SPacket packet, CallbackInfo ci) {
        ((EndRespawningEntity)this.player).setEndRespawning(false);
    }
}
