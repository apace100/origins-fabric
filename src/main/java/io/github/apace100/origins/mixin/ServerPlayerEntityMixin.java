package io.github.apace100.origins.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.power.ModifyDamageTakenPower;
import io.github.apace100.origins.power.NetherSpawnPower;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

    @Shadow public abstract void sendMessage(Text message, boolean actionBar);

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @ModifyArg(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private float modifyDamageAmount(DamageSource source, float originalAmount) {
        return OriginComponent.modify(this, ModifyDamageTakenPower.class, originalAmount, p -> p.doesApply(source, originalAmount));
    }

    // FRESH_AIR
    @Inject(at = @At("HEAD"), method = "trySleep", cancellable = true)
    public void preventAvianSleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> info) {
        OriginComponent.getPowers(this, PreventSleepPower.class).forEach(p -> {
                if(p.doesPrevent(world, pos)) {
                    info.setReturnValue(Either.left(null));
                    this.sendMessage(new TranslatableText(p.getMessage()), true);
                }
            }
        );
    }

    @Inject(at = @At("HEAD"), method = "getSpawnPointDimension", cancellable = true)
    private void modifyBlazebornSpawnDimension(CallbackInfoReturnable<RegistryKey<World>> info) {
        if ((spawnPointPosition == null || hasObstructedSpawn()) && OriginComponent.getPowers(this, NetherSpawnPower.class).size() > 0) {
            info.setReturnValue(World.NETHER);
        }
    }

    @Inject(at = @At("HEAD"), method = "getSpawnPointPosition", cancellable = true)
    private void modifyBlazebornSpawnPosition(CallbackInfoReturnable<BlockPos> info) {
        if(OriginComponent.getPowers(this, NetherSpawnPower.class).size() > 0) {
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
        if((spawnPointPosition == null || hasObstructedSpawn()) && OriginComponent.getPowers(this, NetherSpawnPower.class).size() > 0) {
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

    private BlockPos findNetherSpawn() {
        NetherSpawnPower power = OriginComponent.getPowers(this, NetherSpawnPower.class).get(0);
        Pair<ServerWorld, BlockPos> spawn = power.getSpawn(true);
        if(spawn != null) {
            return spawn.getRight();
        }
        return null;
    }
}
