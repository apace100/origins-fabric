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

public class EndSpawnPower extends Power {

    public EndSpawnPower(PowerType<?> type, PlayerEntity player) {
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
                        Origins.LOGGER.warn("Could not spawn player with EndSpawnPower in the end.");
                    }
                }
            }
        }
    }

    @Override
    public void onRemoved() {
        if(player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            if(serverPlayer.getSpawnPointPosition() != null && serverPlayer.isSpawnPointSet() && serverPlayer.getSpawnPointDimension() == World.END) {
                serverPlayer.setSpawnPoint(World.OVERWORLD, null, 0F, false, false);
            }
        }
    }

    public Pair<ServerWorld, BlockPos> getSpawn(boolean isSpawnObstructed) {
        if(player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            ServerWorld end = serverPlayer.getServerWorld().getServer().getWorld(World.END);
            BlockPos regularSpawn = serverPlayer.getServer().getWorld(World.OVERWORLD).getSpawnPos();
            if(end == null) {
                Origins.LOGGER.error("While looking for a spawn in the End, the End was null.");
                return new Pair<>(serverPlayer.getServer().getWorld(World.OVERWORLD), regularSpawn);
            }
            BlockPos spawnToEndPos = new BlockPos(regularSpawn.getX() / 8, regularSpawn.getY(), regularSpawn.getZ() / 8);
            int iterations = (end.getDimensionHeight() / 2) - 8;
            int center = end.getDimensionHeight() / 2;
            BlockPos.Mutable mutable = spawnToEndPos.mutableCopy();
            mutable.setY(center);
            Vec3d tpPos = getSpawn(end, mutable);
            int range = 64;
            for(int dx = -range; dx <= range && tpPos == null; dx++) {
                for(int dz = -range; dz <= range && tpPos == null; dz++) {
                    for(int i = 1; i < iterations; i++) {
                        mutable.setX(spawnToEndPos.getX() + dx);
                        mutable.setZ(spawnToEndPos.getZ() + dz);
                        mutable.setY(center + i);
                        tpPos = getSpawn(end, mutable);
                        if(tpPos != null) {
                            break;
                        }
                        mutable.setY(center - i);
                        tpPos = getSpawn(end, mutable);
                        if(tpPos != null) {
                            break;
                        }
                    }
                }
            }

            if(tpPos != null) {
                BlockPos endSpawn = mutable;
                end.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(endSpawn), 11, Unit.INSTANCE);
                return new Pair(end, endSpawn);
            } else {
                Origins.LOGGER.warn("Could not find spawn for player with EndSpawnPower in the end in range " + range + " of " + spawnToEndPos.toString() + ".");
            }
        }
        return null;
    }

    private Vec3d getSpawn(ServerWorld world, BlockPos pos) {
        return Dismounting.method_30769(EntityType.PLAYER, world, pos, true);
        //return PlayerEntity.findRespawnPosition(world, pos, 0f, true, false).orElse(null);
    }
}
