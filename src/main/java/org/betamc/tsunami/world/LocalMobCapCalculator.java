package org.betamc.tsunami.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.Chunk;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EnumCreatureType;
import net.minecraft.server.MathHelper;
import net.minecraft.server.World;
import org.bukkit.craftbukkit.util.LongHash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class LocalMobCapCalculator {

    private final Long2ObjectMap<List<EntityPlayer>> playersNearChunk = new Long2ObjectOpenHashMap<>();
    private final Object2ObjectMap<EntityPlayer, MobCounts> playerMobCounts = new Object2ObjectOpenHashMap<>();

    public void prepare(World world) {
        this.playersNearChunk.clear();
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
                    addPlayerToChunk(x + dx, z + dz, (EntityPlayer) player);
                    Chunk chunk = world.getChunkAt(x + dx, z + dz);

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
        List<EntityPlayer> nearbyPlayers = getNearbyPlayers(chunk.x, chunk.z);
        for (int i = 0; i < nearbyPlayers.size(); i++) {
            EntityPlayer player = nearbyPlayers.get(i);
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
