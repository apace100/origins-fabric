package io.github.apace100.origins.power;

import io.github.apace100.origins.Origins;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public class NetherSpawnPower extends Power {

    private static BlockPos netherSpawnPosCache;
    private static Vec3d netherSpawnTpCache;

    public NetherSpawnPower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }

    @Override
    public void onAdded() {
        if(player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            if(!serverPlayer.isSpawnPointSet()) {
                ServerWorld nether = serverPlayer.getServerWorld().getServer().getWorld(World.NETHER);
                if(netherSpawnPosCache != null) {
                    serverPlayer.setSpawnPoint(World.NETHER, netherSpawnPosCache, true, false);
                    serverPlayer.teleport(nether, netherSpawnTpCache.x, netherSpawnTpCache.y, netherSpawnTpCache.z, player.pitch, player.yaw);
                } else {
                    BlockPos spawnToNetherPos = new BlockPos(serverPlayer.getPos().multiply(1.0 / 8.0).subtract(0, 0, 0));
                    int iterations = (nether.getDimensionHeight() / 3) - 4;
                    int center = nether.getDimensionHeight() / 2;
                    BlockPos.Mutable mutable = spawnToNetherPos.mutableCopy();
                    Optional<Vec3d> tpPos = Optional.empty();
                    for(int dx = -32; dx <= 32 && !tpPos.isPresent(); dx++) {
                        for(int dz = -32; dz <= 32 && !tpPos.isPresent(); dz++) {
                            for(int i = 1; i < iterations; i++) {
                                mutable.setY(center + i);
                                tpPos = BedBlock.canWakeUpAt(EntityType.PLAYER, nether, mutable);
                                if(tpPos.isPresent()) {
                                    break;
                                }
                                mutable.setY(center - i);
                                tpPos = BedBlock.canWakeUpAt(EntityType.PLAYER, nether, mutable);
                                if(tpPos.isPresent()) {
                                    break;
                                }
                            }
                        }
                    }

                    if(tpPos.isPresent()) {
                        BlockPos netherSpawn = mutable;
                        serverPlayer.setSpawnPoint(World.NETHER, netherSpawn, true, false);
                        serverPlayer.teleport(nether, tpPos.get().x, tpPos.get().y, tpPos.get().z, player.pitch, player.yaw);
                        netherSpawnPosCache = netherSpawn;
                        netherSpawnTpCache = tpPos.get();
                    } else {
                        Origins.LOGGER.warn("Could not spawn player with NetherSpawnPower in the nether.");
                    }
                }
            }
        }
    }
}
