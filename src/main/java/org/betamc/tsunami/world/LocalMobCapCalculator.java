package org.betamc.tsunami.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.Chunk;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EnumCreatureType;
import org.bukkit.craftbukkit.util.LongHash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalMobCapCalculator {

    private final Long2ObjectMap<List<EntityPlayer>> playersNearChunk = new Long2ObjectOpenHashMap<>();
    private final Map<EntityPlayer, MobCounts> playerMobCounts = new HashMap<>();

    private List<EntityPlayer> getNearbyPlayers(int x, int z) {
        long chunkPos = LongHash.toLong(x, z);
        return this.playersNearChunk.getOrDefault(chunkPos, Collections.emptyList());
    }

    public LongSet getEligibleChunkPositions() {
        return this.playersNearChunk.keySet();
    }

    public void addPlayerToChunk(int x, int z, EntityPlayer player) {
        long chunkPos = LongHash.toLong(x, z);
        this.playersNearChunk.computeIfAbsent(chunkPos, k -> new ArrayList<>()).add(player);
    }

    public void addMobToNearbyPlayer(EntityPlayer player, EnumCreatureType creatureType) {
        this.playerMobCounts.computeIfAbsent(player, k -> new MobCounts()).add(creatureType);
    }

    public boolean canSpawn(EnumCreatureType creatureType, Chunk chunk) {
        for (EntityPlayer player : getNearbyPlayers(chunk.x, chunk.z)) {
            MobCounts mobCounts = this.playerMobCounts.get(player);
            if (mobCounts == null || mobCounts.canSpawn(creatureType)) {
                return true;
            }
        }
        return false;
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
