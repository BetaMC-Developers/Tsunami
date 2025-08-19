package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import net.minecraft.server.TileEntityMobSpawner;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.CreatureType;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class CraftCreatureSpawner extends CraftBlockState implements CreatureSpawner {
    private final CraftWorld world;
    private final TileEntityMobSpawner spawner;

    public CraftCreatureSpawner(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        spawner = (TileEntityMobSpawner) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public CreatureType getCreatureType() {
        return CreatureType.fromName(spawner.mobName);
    }

    public void setCreatureType(CreatureType creatureType) {
        spawner.mobName = creatureType.getName();
    }

    public String getCreatureTypeId() {
        return spawner.mobName;
    }

    public void setCreatureTypeId(String creatureType) {
        // Verify input
        CreatureType type = CreatureType.fromName(creatureType);
        if (type == null) {
            return;
        }
        spawner.mobName = type.getName();
    }

    public int getDelay() {
        return spawner.spawnDelay;
    }

    public void setDelay(int delay) {
        spawner.spawnDelay = delay;
    }

    // Tsunami start
    public void setMetadata(Plugin owningPlugin, String key, MetadataValue value) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");
        Preconditions.checkArgument(value != null, "value cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        spawner.metadataStore.put(fullKey, value);
    }

    public void removeMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        spawner.metadataStore.remove(fullKey);
    }

    public MetadataValue getMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        return spawner.metadataStore.get(fullKey);
    }

    public boolean hasMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        return spawner.metadataStore.containsKey(fullKey);
    }
    // Tsunami end

}
