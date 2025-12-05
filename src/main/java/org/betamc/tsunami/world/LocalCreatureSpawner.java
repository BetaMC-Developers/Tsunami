package org.betamc.tsunami.world;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.BiomeMeta;
import net.minecraft.server.Block;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.EntitySpider;
import net.minecraft.server.EnumCreatureType;
import net.minecraft.server.Material;
import net.minecraft.server.MathHelper;
import net.minecraft.server.World;
import net.minecraft.server.WorldServer;
import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Collection;
import java.util.List;

public class LocalCreatureSpawner {

    private LocalCreatureSpawner() {
    }

    public static void spawnCreatures(WorldServer world, boolean spawnMonsters, boolean spawnAnimals) {
        LocalMobCapCalculator calculator = createCalculator(world);
        Collection<Chunk> chunks = world.chunkProviderServer.chunks.values();
        chunks.forEach(chunk -> spawnForChunk(world, chunk, calculator, spawnMonsters, spawnAnimals));
    }

    private static void spawnForChunk(World world, Chunk chunk, LocalMobCapCalculator calculator, boolean spawnMonsters, boolean spawnAnimals) {
        for (EnumCreatureType creatureType : EnumCreatureType.values()) {
            if ((!creatureType.d() || spawnAnimals) && (creatureType.d() || spawnMonsters) && calculator.canSpawn(creatureType, chunk)) {
                spawnCreatureTypeForChunk(creatureType, world, chunk, calculator);
            }
        }
    }

    private static void spawnCreatureTypeForChunk(EnumCreatureType creatureType, World world, Chunk chunk, LocalMobCapCalculator calculator) {
        int x = (chunk.x << 4) + world.random.nextInt(16);
        int y = world.random.nextInt(128);
        int z = (chunk.z << 4) + world.random.nextInt(16);

        if (!isFullBlock(chunk, x, y, z) && getMaterial(chunk, x, y, z) == creatureType.c()) {
            BiomeMeta mob = getWeightedRandomMob(creatureType, world, chunk);
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

                    if (hasDistanceToPlayersAndSpawn(world, randX, y, randZ) && isValidSpawnPosition(creatureType, currentChunk, randX, y, randZ)) {
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

    private static LocalMobCapCalculator createCalculator(World world) {
        LocalMobCapCalculator calculator = new LocalMobCapCalculator(world);
        for (int i = 0; i < world.entityList.size(); i++) {
            Entity entity = (Entity) world.entityList.get(i);
            EnumCreatureType creatureType = getCreatureType(entity);
            if (creatureType != null) {
                int x = MathHelper.floor(entity.locX) >> 4;
                int z = MathHelper.floor(entity.locZ) >> 4;
                calculator.addMob(x, z, creatureType);
            }
        }

        return calculator;
    }

    private static EnumCreatureType getCreatureType(Entity entity) {
        for (EnumCreatureType creatureType : EnumCreatureType.values()) {
            if (creatureType.a().isAssignableFrom(entity.getClass())) {
                return creatureType;
            }
        }
        return null;
    }

    private static boolean isFullBlock(Chunk chunk, int x, int y, int z) {
        Block block = Block.byId[chunk.getTypeId(x & 15, y, z & 15)];
        return block != null && block.material.h() && block.b();
    }

    private static Material getMaterial(Chunk chunk, int x, int y, int z) {
        int id = chunk.getTypeId(x & 15, y, z & 15);
        return id == 0 ? Material.AIR : Block.byId[id].material;
    }

    private static BiomeMeta getWeightedRandomMob(EnumCreatureType creatureType, World world, Chunk chunk) {
        BiomeBase biome = world.getWorldChunkManager().a(LongHash.toLong(chunk.x, chunk.z));
        List mobs = biome.a(creatureType);
        if (mobs == null || mobs.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (Object obj : mobs) {
            totalWeight += ((BiomeMeta) obj).b;
        }

        int rand = world.random.nextInt(totalWeight);
        BiomeMeta mob = (BiomeMeta) mobs.get(0);
        for (Object obj : mobs) {
            BiomeMeta otherMob = (BiomeMeta) obj;
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
