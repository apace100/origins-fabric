package io.github.apace100.origins.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import io.github.apace100.origins.access.EndRespawningEntity;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ModifyDamageTakenPower;
import io.github.apace100.origins.power.ModifyPlayerSpawnPower;
import io.github.apace100.origins.power.PreventSleepPower;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ScreenHandlerListener, EndRespawningEntity {

    @Shadow private RegistryKey<World> spawnPointDimension;

    @Shadow
    private BlockPos spawnPointPosition;

    @Shadow private boolean spawnPointSet;

    @Shadow @Final public MinecraftServer server;

    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Shadow public abstract void sendMessage(Text message, boolean actionBar);

    @Shadow public boolean notInAnyWorld;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @ModifyArg(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private float modifyDamageAmount(DamageSource source, float originalAmount) {
        return OriginComponent.modify(this, ModifyDamageTakenPower.class, originalAmount, p -> p.doesApply(source, originalAmount), p -> p.executeActions(source.getAttacker()));
    }

    // FRESH_AIR
    @Inject(method = "trySleep", at = @At(value = "INVOKE",target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/util/math/BlockPos;FZZ)V"), cancellable = true)
    public void preventAvianSleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> info) {
        OriginComponent.getPowers(this, PreventSleepPower.class).forEach(p -> {
                if(p.doesPrevent(world, pos)) {
                    if(p.doesAllowSpawnPoint()) {
                        ((ServerPlayerEntity)(Object)this).setSpawnPoint(this.world.getRegistryKey(), pos, this.yaw, false, true);
                    }
                    info.setReturnValue(Either.left(null));
                    this.sendMessage(new TranslatableText(p.getMessage()), true);
                }
            }
        );
    }

    @Inject(at = @At("HEAD"), method = "getSpawnPointDimension", cancellable = true)
    private void modifySpawnPointDimension(CallbackInfoReturnable<RegistryKey<World>> info) {
        if (!this.origins_isEndRespawning && (spawnPointPosition == null || hasObstructedSpawn()) && OriginComponent.getPowers(this, ModifyPlayerSpawnPower.class).size() > 0) {
            ModifyPlayerSpawnPower power = OriginComponent.getPowers(this, ModifyPlayerSpawnPower.class).get(0);
            info.setReturnValue(power.dimension);
        }
    }

    @Inject(at = @At("HEAD"), method = "getSpawnPointPosition", cancellable = true)
    private void modifyPlayerSpawnPosition(CallbackInfoReturnable<BlockPos> info) {
        if(!this.origins_isEndRespawning && OriginComponent.getPowers(this, ModifyPlayerSpawnPower.class).size() > 0) {
            if(spawnPointPosition == null) {
                info.setReturnValue(findPlayerSpawn());
            } else if(hasObstructedSpawn()) {
                networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, 0.0F));
                info.setReturnValue(findPlayerSpawn());
            }
        }
    }


    @Inject(at = @At("HEAD"), method = "isSpawnPointSet", cancellable = true)
    private void modifySpawnPointSet(CallbackInfoReturnable<Boolean> info) {
        if(!this.origins_isEndRespawning && (spawnPointPosition == null || hasObstructedSpawn()) && OriginComponent.hasPower(this, ModifyPlayerSpawnPower.class)) {
            info.setReturnValue(true);
        }
    }

    private boolean hasObstructedSpawn() {
        ServerWorld world = server.getWorld(spawnPointDimension);
        if(spawnPointPosition != null && world != null) {
            Optional optional = PlayerEntity.findRespawnPosition(world, spawnPointPosition, 0F, spawnPointSet, true);
            return !optional.isPresent();
        }
        return false;
    }

    private BlockPos findPlayerSpawn() {
        ModifyPlayerSpawnPower power = OriginComponent.getPowers(this, ModifyPlayerSpawnPower.class).get(0);
        Pair<ServerWorld, BlockPos> spawn = power.getSpawn(true);
        if(spawn != null) {
            return spawn.getRight();
        }
        return null;
    }

    @Unique
    private boolean origins_isEndRespawning;

    @Override
    public void setEndRespawning(boolean endSpawn) {
        this.origins_isEndRespawning = endSpawn;
    }

    @Override
    public boolean isEndRespawning() {
        return this.origins_isEndRespawning;
    }

    @Override
    public boolean hasRealRespawnPoint() {
        return spawnPointPosition != null && !hasObstructedSpawn();
    }
}
