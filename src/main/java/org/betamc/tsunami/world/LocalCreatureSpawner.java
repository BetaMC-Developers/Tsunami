package org.betamc.tsunami.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.BiomeMeta;
import net.minecraft.server.Block;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.EntitySpider;
import net.minecraft.server.EnumCreatureType;
import net.minecraft.server.Material;
import net.minecraft.server.World;
import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.List;

public class LocalCreatureSpawner {

    private static final LocalMobCapCalculator calculator = new LocalMobCapCalculator();
    private static final LongOpenHashSet chunks = new LongOpenHashSet();

    private LocalCreatureSpawner() {
    }

    public static void spawnCreatures(World world, boolean spawnMonsters, boolean spawnAnimals) {
        chunks.clear();
        calculator.prepare(world);
        calculator.forEachEntry((player, chunks) -> spawnForPlayer(world, player, chunks, spawnMonsters, spawnAnimals));
    }

    private static void spawnForPlayer(World world, EntityPlayer player, List<Chunk> chunksNearPlayer, boolean spawnMonsters, boolean spawnAnimals) {
        for (int i = 0; i < EnumCreatureType.types().length; i++) {
            EnumCreatureType creatureType = EnumCreatureType.types()[i];
            if ((!creatureType.d() || spawnAnimals) && (creatureType.d() || spawnMonsters) && calculator.canSpawnForPlayer(creatureType, player)) {
                chunksNearPlayer.forEach(chunk -> spawnCreatureTypeForChunk(creatureType, world, chunk));
            }
        }
    }

    private static void spawnCreatureTypeForChunk(EnumCreatureType creatureType, World world, Chunk chunk) {
        if (!chunks.add(LongHash.toLong(chunk.x, chunk.z))) return;
        int x = (chunk.x << 4) + world.random.nextInt(16);
        int y = world.random.nextInt(128);
        int z = (chunk.z << 4) + world.random.nextInt(16);

        if (!isFullBlock(chunk, x, y, z) && getMaterial(chunk, x, y, z) == creatureType.c()) {
            BiomeMeta mob = getWeightedRandomMob(creatureType, world, chunk);
            if (mob == null) return;
            Chunk currentChunk;
            int count = 0;
            int radius = 6;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 4; j++) {
                    currentChunk = chunk;
                    int randX = x + world.random.nextInt(radius) - world.random.nextInt(radius);
                    int randZ = z + world.random.nextInt(radius) - world.random.nextInt(radius);
                    if (randX >> 4 != x >> 4 || randZ >> 4 != z >> 4) {
                        currentChunk = world.getChunkAt(randX, randZ);
                    }

                    if (isValidSpawnPosition(creatureType, currentChunk, randX, y, randZ) && hasDistanceToPlayersAndSpawn(world, randX, y, randZ)) {
                        EntityLiving entity;
                        try {
                            entity = (EntityLiving) mob.a.getConstructor(World.class).newInstance(world);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        entity.setPositionRotation(randX + 0.5, y, randZ + 0.5, world.random.nextFloat() * 360.0f, 0.0f);
                        if (entity.d()) {
                            count++;
                            world.addEntity(entity, CreatureSpawnEvent.SpawnReason.NATURAL);
                            postSpawn(entity, world);
                            if (count >= entity.l()) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private static int getTypeId(Chunk chunk, int x, int y, int z) {
        if (y >= 0 && y < 128) {
            return chunk.getTypeId(x & 15, y, z & 15);
        } else {
            return 0;
        }
    }

    private static boolean isFullBlock(Chunk chunk, int x, int y, int z) {
        Block block = Block.byId[getTypeId(chunk, x & 15, y, z & 15)];
        return block != null && block.material.h() && block.b();
    }

    private static Material getMaterial(Chunk chunk, int x, int y, int z) {
        int id = getTypeId(chunk, x & 15, y, z & 15);
        return id == 0 ? Material.AIR : Block.byId[id].material;
    }

    private static BiomeMeta getWeightedRandomMob(EnumCreatureType creatureType, World world, Chunk chunk) {
        BiomeBase biome = world.getWorldChunkManager().a(LongHash.toLong(chunk.x, chunk.z));
        List mobs = biome.a(creatureType);
        if (mobs == null || mobs.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (int i = 0; i < mobs.size(); i++) {
            BiomeMeta mob = (BiomeMeta) mobs.get(i);
            totalWeight += mob.b;
        }

        int rand = world.random.nextInt(totalWeight);
        BiomeMeta mob = (BiomeMeta) mobs.get(0);
        for (int i = 0; i < mobs.size(); i++) {
            BiomeMeta otherMob = (BiomeMeta) mobs.get(i);
            rand -= otherMob.b;
            if (rand < 0) {
                mob = otherMob;
                break;
            }
        }

        return mob;
    }

    private static boolean isValidSpawnPosition(EnumCreatureType creatureType, Chunk chunk, int x, int y, int z) {
        if (creatureType.c() == Material.WATER) {
            return getMaterial(chunk, x, y, z).isLiquid() && !isFullBlock(chunk, x, y + 1, z);
        } else {
            return isFullBlock(chunk, x, y - 1, z) && !isFullBlock(chunk, x, y, z) && !getMaterial(chunk, x, y, z).isLiquid() && !isFullBlock(chunk, x, y + 1, z);
        }
    }

    private static boolean hasDistanceToPlayersAndSpawn(World world, int x, int y, int z) {
        float centerX = x + 0.5f;
        float centerZ = z + 0.5f;
        ChunkCoordinates spawn = world.getSpawn();
        float dx = centerX - spawn.x;
        float dy = y - spawn.y;
        float dz = centerZ - spawn.z;
        float distanceToSpawn = dx * dx + dy * dy + dz * dz;

        if (distanceToSpawn >= 576.0f) {
            return world.a(centerX, y, centerZ, 24.0) == null;
        } else {
            return false;
        }
    }

    private static void postSpawn(EntityLiving entity, World world) {
        if (entity instanceof EntitySpider && world.random.nextInt(100) == 0) {
            EntitySkeleton skeleton = new EntitySkeleton(world);

            skeleton.setPositionRotation(entity.locX, entity.locY - entity.height, entity.locZ, entity.yaw, 0.0f);
            world.addEntity(skeleton, CreatureSpawnEvent.SpawnReason.NATURAL);
            skeleton.mount(entity);
        } else if (entity instanceof EntitySheep) {
            ((EntitySheep) entity).setColor(EntitySheep.a(world.random));
        }
    }

}
