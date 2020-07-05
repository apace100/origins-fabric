package io.github.apace100.origins.power;

import io.github.apace100.origins.Origins;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public class NetherSpawnPower extends Power {

    public NetherSpawnPower(PowerType<?> type, PlayerEntity player) {
        super(type, player);
    }

    @Override
    public void onChosen() {
        if(player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            Pair<ServerWorld, BlockPos> spawn = getSpawn(false);
            if(spawn != null) {
                serverPlayer.setSpawnPoint(World.NETHER, spawn.getRight(), true, false);
                Optional<Vec3d> tpPos = BedBlock.canWakeUpAt(EntityType.PLAYER, spawn.getLeft(), spawn.getRight());
                if(tpPos.isPresent()) {
                    serverPlayer.teleport(spawn.getLeft(), tpPos.get().x, tpPos.get().y, tpPos.get().z, player.pitch, player.yaw);
                } else {
                    Origins.LOGGER.warn("Could not spawn player with NetherSpawnPower in the nether.");
                }
            }
        }
    }

    public Pair<ServerWorld, BlockPos> getSpawn(boolean isSpawnObstructed) {
        if(player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            ServerWorld nether = serverPlayer.getServerWorld().getServer().getWorld(World.NETHER);
            BlockPos regularSpawn = serverPlayer.getServer().getWorld(World.OVERWORLD).getSpawnPos();
            BlockPos spawnToNetherPos = new BlockPos(regularSpawn.getX() / 8, regularSpawn.getY(), regularSpawn.getZ() / 8);
            int iterations = (nether.getDimensionHeight() / 2) - 8;
            int center = nether.getDimensionHeight() / 2;
            BlockPos.Mutable mutable = spawnToNetherPos.mutableCopy();
            mutable.setY(center);
            Optional<Vec3d> tpPos = BedBlock.canWakeUpAt(EntityType.PLAYER, nether, mutable);
            int range = 256;
            for(int dx = -range; dx <= range && !tpPos.isPresent(); dx++) {
                for(int dz = -range; dz <= range && !tpPos.isPresent(); dz++) {
                    for(int i = 1; i < iterations; i++) {
                        mutable.setX(spawnToNetherPos.getX() + dx);
                        mutable.setZ(spawnToNetherPos.getZ() + dz);
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
                nether.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(netherSpawn), 11, Unit.INSTANCE);
                return new Pair(nether, netherSpawn);
            } else {
                Origins.LOGGER.warn("Could not find spawn for player with NetherSpawnPower in the nether in range " + range + " of " + spawnToNetherPos.toString() + ".");
            }
        }
        return null;
    }
}
