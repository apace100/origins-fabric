package io.github.apace100.origins.power;

import io.github.apace100.origins.Origins;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.Optional;

public class ModifyPlayerSpawnPower extends Power {
    public final RegistryKey<World> dimension;
    public final float dimensionDistanceMultiplier;
    public final Identifier biomeId;
    public final String spawnStrategy;
    public final StructureFeature structure;
    public final SoundEvent spawnSound;


    public ModifyPlayerSpawnPower(PowerType<?> type, PlayerEntity player, RegistryKey<World> dimension, float dimensionDistanceMultiplier, Identifier biomeId, String spawnStrategy, StructureFeature<?> structure, SoundEvent spawnSound) {
        super(type, player);
        this.dimension = dimension;
        this.dimensionDistanceMultiplier = dimensionDistanceMultiplier;
        this.biomeId = biomeId;
        this.spawnStrategy = spawnStrategy;
        this.structure = structure;
        this.spawnSound = spawnSound;
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
                        serverPlayer.teleport(spawn.getLeft(), spawn.getRight().getX(), spawn.getRight().getY(), spawn.getRight().getZ(), player.pitch, player.yaw);
                        Origins.LOGGER.warn("Could not spawn player with `ModifySpawnPower` at the desired location.");
                    }
                }
            }
        }
    }

    @Override
    public void onRemoved() {
        if(player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            if(serverPlayer.getSpawnPointPosition() != null && serverPlayer.isSpawnPointSet()) {
                serverPlayer.setSpawnPoint(World.OVERWORLD, null, 0F, false, false);
            }
        }
    }

    public Pair<ServerWorld, BlockPos> getSpawn(boolean isSpawnObstructed) {
        if(player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            ServerWorld world = serverPlayer.getServerWorld().getServer().getWorld(dimension);
            BlockPos regularSpawn = serverPlayer.getServerWorld().getServer().getWorld(World.OVERWORLD).getSpawnPos();
            BlockPos spawnToDimPos;
            int iterations = (world.getDimensionHeight() / 2) - 8;
            int center = world.getDimensionHeight() / 2;
            BlockPos.Mutable mutable;
            Vec3d tpPos;
            int range = 64;

            switch(spawnStrategy) {
                case "center":
                    spawnToDimPos = new BlockPos(0, center, 0);
                    break;

                case "default":
                    if(dimensionDistanceMultiplier != 0) {
                        spawnToDimPos = new BlockPos(regularSpawn.getX() * dimensionDistanceMultiplier, regularSpawn.getY(), regularSpawn.getZ() * dimensionDistanceMultiplier);
                    } else {
                        spawnToDimPos = new BlockPos(regularSpawn.getX(), regularSpawn.getY(), regularSpawn.getZ());
                    }
                    break;

                default:
                    Origins.LOGGER.warn("This case does nothing. The game crashes if there is no spawn strategy set");
                    if(dimensionDistanceMultiplier != 0) {
                        spawnToDimPos = new BlockPos(regularSpawn.getX() * dimensionDistanceMultiplier, regularSpawn.getY(), regularSpawn.getZ() * dimensionDistanceMultiplier);
                    } else {
                        spawnToDimPos = new BlockPos(regularSpawn.getX(), regularSpawn.getY(), regularSpawn.getZ());
                    }
            }

            if(biomeId != null) {
                Optional<Biome> biomeOptional = world.getRegistryManager().get(Registry.BIOME_KEY).getOrEmpty(biomeId);
                if(biomeOptional.isPresent()) {
                    BlockPos biomePos = world.locateBiome(biomeOptional.get(), spawnToDimPos, 6400, 8);
                    if(biomePos != null) {
                        spawnToDimPos = biomePos;
                    } else {
                        Origins.LOGGER.warn("Could not find biome \"" + biomeId.toString() + "\" in dimension \"" + dimension.toString() + "\".");
                    }
                } else {
                    Origins.LOGGER.warn("Biome with ID \"" + biomeId.toString() + "\" was not registered.");
                }
            }

            if(structure == null) {
                tpPos = getValidSpawn(spawnToDimPos, range, world);
            } else {
                BlockPos structurePos = getStructureLocation(structure, dimension);
                ChunkPos structureChunkPos;

                if(structurePos == null) {
                    return null;
                }
                structureChunkPos = new ChunkPos(structurePos.getX() >> 4, structurePos.getZ() >> 4);
                StructureStart structureStart = world.getStructureAccessor().getStructureStart(ChunkSectionPos.from(structureChunkPos, 0), structure, world.getChunk(structurePos));
                BlockPos structureCenter = new BlockPos(structureStart.getBoundingBox().getCenter());
                tpPos = getValidSpawn(structureCenter, range, world);
            }

            if(tpPos != null) {
                mutable = new BlockPos(tpPos.x, tpPos.y, tpPos.z).mutableCopy();
                BlockPos spawnLocation = mutable;
                world.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(spawnLocation), 11, Unit.INSTANCE);
                return new Pair(world, spawnLocation);
            }
            return null;
        }
        return null;
    }

    private BlockPos getStructureLocation(StructureFeature structure, RegistryKey<World> dimension) {
        BlockPos blockPos = new BlockPos(0, 70, 0);
        ServerWorld serverWorld = player.getServer().getWorld(dimension);
        BlockPos blockPos2 = serverWorld.locateStructure(structure, blockPos, 100, false);
        //FrostburnOrigins.LOGGER.warn("Unrecognized dimension id '" + dimensionId + "', defaulting to id '0', OVERWORLD");
        if (blockPos2 == null) {
            Origins.LOGGER.warn("Could not find '" + structure.getName() + "' in dimension: " + dimension.getValue());
            return null;
        } else {
            return blockPos2;
        }
    }

    private Vec3d getValidSpawn(BlockPos startPos, int range, ServerWorld world) {
        // (di, dj) is a vector - direction in which we move right now
        int dx = 1;
        int dz = 0;
        // length of current segment
        int segmentLength = 1;
        BlockPos.Mutable mutable = startPos.mutableCopy();
        // center of our starting structure, or dimension
        int center = startPos.getY();
        // Our valid spawn location
        Vec3d tpPos;

        // current position (x, z) and how much of current segment we passed
        int x = startPos.getX();
        int z = startPos.getZ();
        //position to check up, or down
        int segmentPassed = 0;
        // increase y check
        int i = 0;
        // Decrease y check
        int d = 0;
        while(i < world.getDimensionHeight() || d > 0) {
            for (int coordinateCount = 0; coordinateCount < range; ++coordinateCount) {
                // make a step, add 'direction' vector (di, dj) to current position (i, j)
                x += dx;
                z += dz;
                ++segmentPassed;mutable.setX(x);
                mutable.setZ(z);
                mutable.setY(center + i);
                tpPos = Dismounting.method_30769(EntityType.PLAYER, world, mutable, true);
                if (tpPos != null) {
                    return(tpPos);
                } else {
                    mutable.setY(center + d);
                    tpPos = Dismounting.method_30769(EntityType.PLAYER, world, mutable, true);
                    if (tpPos != null) {
                        return(tpPos);
                    }
                }

                if (segmentPassed == segmentLength) {
                    // done with current segment
                    segmentPassed = 0;

                    // 'rotate' directions
                    int buffer = dx;
                    dx = -dz;
                    dz = buffer;

                    // increase segment length if necessary
                    if (dz == 0) {
                        ++segmentLength;
                    }
                }
            }
            i++;
            d--;
        }
        return(null);
    }
}

