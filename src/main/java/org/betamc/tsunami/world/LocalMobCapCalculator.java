package org.betamc.tsunami.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.Chunk;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EnumCreatureType;
import net.minecraft.server.World;
import org.bukkit.craftbukkit.util.LongHash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalMobCapCalculator {

    private final Long2ObjectMap<List<EntityPlayer>> playersNearChunk = new Long2ObjectOpenHashMap<>();
    private final Map<EntityPlayer, MobCounts> playerMobCounts = new HashMap<>();
    private final World world;

    public LocalMobCapCalculator(World world) {
        this.world = world;
    }

    private List<EntityPlayer> getNearbyPlayers(int x, int z) {
        long chunkPos = LongHash.toLong(x, z);
        return this.playersNearChunk.computeIfAbsent(chunkPos, this.world::getNearbyPlayersForSpawning);
    }

    public void addMob(int x, int z, EnumCreatureType creatureType) {
        for (EntityPlayer player : getNearbyPlayers(x, z)) {
            this.playerMobCounts.computeIfAbsent(player, k -> new MobCounts()).add(creatureType);
        }
    }

    public boolean canSpawn(EnumCreatureType creatureType, Chunk chunk) {
        for (EntityPlayer player : getNearbyPlayers(chunk.x, chunk.z)) {
            LocalMobCapCalculator.MobCounts mobCounts = this.playerMobCounts.get(player);
            if (mobCounts == null || mobCounts.canSpawn(creatureType)) {
                return true;
            }
        }
        return false;
    }

    private static class MobCounts {
        private final Object2IntMap<EnumCreatureType> counts = new Object2IntOpenHashMap<>();

        public void add(EnumCreatureType creatureType) {
            this.counts.computeInt(creatureType, (k, v) -> v == null ? 1 : v + 1);
        }

        public boolean canSpawn(EnumCreatureType creatureType) {
            return this.counts.getOrDefault(creatureType, 0) < creatureType.b();
        }
    }

}
