package org.betamc.tsunami.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.Chunk;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EnumCreatureType;
import net.minecraft.server.MathHelper;
import net.minecraft.server.World;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class LocalMobCapCalculator {

    private final Object2ObjectMap<EntityPlayer, ObjectArrayList<Chunk>> chunksNearPlayer = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<EntityPlayer, MobCounts> playerMobCounts = new Object2ObjectOpenHashMap<>();

    public void prepare(World world) {
        this.chunksNearPlayer.clear();
        this.playerMobCounts.clear();

        for (int i = 0; i < world.players.size(); i++) {
            EntityHuman player = (EntityHuman) world.players.get(i);
            if (!(player instanceof EntityPlayer) || player.dead) {
                continue;
            }

            int x = MathHelper.floor(player.locX) >> 4;
            int z = MathHelper.floor(player.locZ) >> 4;
            byte radius = 8;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Chunk chunk = world.getChunkAt(x + dx, z + dz);
                    addChunkToPlayer((EntityPlayer) player, chunk);

                    for (int j = 0; j < chunk.entitySlices.length; j++) {
                        List slice = chunk.entitySlices[j];
                        for (int k = 0; k < slice.size(); k++) {
                            Entity entity = (Entity) slice.get(k);
                            EnumCreatureType creatureType = getCreatureType(entity);
                            if (creatureType != null) {
                                addMobToNearbyPlayer((EntityPlayer) player, creatureType);
                            }
                        }
                    }
                }
            }
        }
    }

    private static EnumCreatureType getCreatureType(Entity entity) {
        for (int i = 0; i < EnumCreatureType.types().length; i++) {
            EnumCreatureType creatureType = EnumCreatureType.types()[i];
            if (creatureType.a().isAssignableFrom(entity.getClass())) {
                return creatureType;
            }
        }
        return null;
    }

    public void forEachEntry(BiConsumer<EntityPlayer, ObjectArrayList<Chunk>> consumer) {
        this.chunksNearPlayer.forEach(consumer);
    }

    public void addChunkToPlayer(EntityPlayer player, Chunk chunk) {
        this.chunksNearPlayer.computeIfAbsent(player, k -> new ObjectArrayList<>(289)).add(chunk);
    }

    public void addMobToNearbyPlayer(EntityPlayer player, EnumCreatureType creatureType) {
        this.playerMobCounts.computeIfAbsent(player, k -> new MobCounts()).add(creatureType);
    }

    public boolean canSpawnForPlayer(EnumCreatureType creatureType, EntityPlayer player) {
        MobCounts mobCounts = this.playerMobCounts.get(player);
        return mobCounts == null || mobCounts.canSpawn(creatureType);
    }

    private static class MobCounts {
        private final Map<EnumCreatureType, Integer> counts = new EnumMap<>(EnumCreatureType.class);

        private void add(EnumCreatureType creatureType) {
            this.counts.compute(creatureType, (k, v) -> v == null ? 1 : v + 1);
        }

        private boolean canSpawn(EnumCreatureType creatureType) {
            return this.counts.getOrDefault(creatureType, 0) < creatureType.b();
        }
    }

}
