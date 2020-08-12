package io.github.apace100.origins.power;

import io.github.apace100.origins.Origins;
import net.minecraft.entity.Dismounting;
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
    public void onChosen(boolean isOrbOfOrigin) {
        if(player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            Pair<ServerWorld, BlockPos> spawn = getSpawn(false);
            if(spawn != null) {
                if(!isOrbOfOrigin) {
                    Vec3d tpPos = Dismounting.method_30769(EntityType.PLAYER, spawn.getLeft(), spawn.getRight(), true);
                    if(tpPos != null) {
                        serverPlayer.teleport(spawn.getLeft(), tpPos.x, tpPos.y, tpPos.z, player.pitch, player.yaw);
                    } else {
                        Origins.LOGGER.warn("Could not spawn player with NetherSpawnPower in the nether.");
                    }
                }
            }
        }
    }

    @Override
    public void onRemoved() {
        if(player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            if(serverPlayer.getSpawnPointPosition() != null && serverPlayer.isSpawnPointSet() && serverPlayer.getSpawnPointDimension() == World.NETHER) {
                serverPlayer.setSpawnPoint(World.OVERWORLD, null, 0F, false, false);
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
            Vec3d tpPos = Dismounting.method_30769(EntityType.PLAYER, nether, mutable, true);
            int range = 64;
            for(int dx = -range; dx <= range && tpPos == null; dx++) {
                for(int dz = -range; dz <= range && tpPos == null; dz++) {
                    for(int i = 1; i < iterations; i++) {
                        mutable.setX(spawnToNetherPos.getX() + dx);
                        mutable.setZ(spawnToNetherPos.getZ() + dz);
                        mutable.setY(center + i);
                        tpPos = Dismounting.method_30769(EntityType.PLAYER, nether, mutable, true);
                        if(tpPos != null) {
                            break;
                        }
                        mutable.setY(center - i);
                        tpPos = Dismounting.method_30769(EntityType.PLAYER, nether, mutable, true);
                        if(tpPos != null) {
                            break;
                        }
                    }
                }
            }

            if(tpPos != null) {
                BlockPos netherSpawn = mutable;
                nether.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(netherSpawn), 11, Unit.INSTANCE);
                return new Pair(nether, netherSpawn);
            } else {
                Origins.LOGGER.warn("Could not find spawn for player with NetherSpawnPower in the nether in range " + range + " of " + spawnToNetherPos.toString() + ".");
            }
        }
        return null;
    }

    private Optional<Vec3d> getValidPosition(World world, BlockPos pos) {
         Vec3d p = Dismounting.method_30769(EntityType.PLAYER, world, pos, false);
         return Optional.of(p);
    }
}
