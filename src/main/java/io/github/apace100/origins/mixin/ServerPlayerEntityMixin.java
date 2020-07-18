package io.github.apace100.origins.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import io.github.apace100.origins.power.PowerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ScreenHandlerListener {

    @Shadow private RegistryKey<World> spawnPointDimension;

    @Shadow
    private BlockPos spawnPointPosition;

    @Shadow private boolean spawnPointSet;

    @Shadow @Final public MinecraftServer server;

    @Shadow public ServerPlayNetworkHandler networkHandler;

    public ServerPlayerEntityMixin(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    // FRESH_AIR
    @Inject(at = @At("HEAD"), method = "trySleep", cancellable = true)
    public void preventAvianSleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> info) {
        if (PowerTypes.FRESH_AIR.isActive(this) && pos.getY() < 86) {
            info.setReturnValue(Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_HERE));
        }
    }

    @Inject(at = @At("HEAD"), method = "getSpawnPointDimension", cancellable = true)
    private void modifyBlazebornSpawnDimension(CallbackInfoReturnable<RegistryKey<World>> info) {
        if ((spawnPointPosition == null || hasObstructedSpawn()) && PowerTypes.NETHER_SPAWN.isActive(this)) {
            info.setReturnValue(World.NETHER);
        }
    }

    @Inject(at = @At("HEAD"), method = "getSpawnPointPosition", cancellable = true)
    private void modifyBlazebornSpawnPosition(CallbackInfoReturnable<BlockPos> info) {
        if(PowerTypes.NETHER_SPAWN.isActive(this)) {
            if(spawnPointPosition == null) {
                info.setReturnValue(findNetherSpawn());
            } else if(hasObstructedSpawn()) {
                networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, 0.0F));
                info.setReturnValue(findNetherSpawn());
            }
        }
    }


    @Inject(at = @At("HEAD"), method = "isSpawnPointSet", cancellable = true)
    private void modifyBlazebornSpawnPointSet(CallbackInfoReturnable<Boolean> info) {
        if((spawnPointPosition == null || hasObstructedSpawn()) && PowerTypes.NETHER_SPAWN.isActive(this)) {
            info.setReturnValue(true);
        }
    }

    private boolean hasObstructedSpawn() {
        ServerWorld world = server.getWorld(spawnPointDimension);
        if(spawnPointPosition != null && world != null) {
            Optional optional = PlayerEntity.findRespawnPosition(world, spawnPointPosition, spawnPointSet, true);
            return !optional.isPresent();
        }
        return false;
    }

    private BlockPos findNetherSpawn() {
        Pair<ServerWorld, BlockPos> spawn = PowerTypes.NETHER_SPAWN.get(this).getSpawn(true);
        if(spawn != null) {
            return spawn.getRight();
        }
        return null;
    }
}
