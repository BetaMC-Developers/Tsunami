package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import net.minecraft.server.TileEntityChest;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class CraftChest extends CraftBlockState implements Chest {
    private final CraftWorld world;
    private final TileEntityChest chest;

    public CraftChest(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        chest = (TileEntityChest) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public Inventory getInventory() {
        return new CraftInventory(chest);
    }

    @Override
    public boolean update(boolean force) {
        boolean result = super.update(force);

        if (result) {
            chest.update();
        }

        return result;
    }

    // Tsunami start
    public void setMetadata(Plugin owningPlugin, String key, MetadataValue value) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");
        Preconditions.checkArgument(value != null, "value cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        chest.metadataStore.put(fullKey, value);
    }

    public void removeMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        chest.metadataStore.remove(fullKey);
    }

    public MetadataValue getMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        return chest.metadataStore.get(fullKey);
    }

    public boolean hasMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        return chest.metadataStore.containsKey(fullKey);
    }
    // Tsunami end

}
