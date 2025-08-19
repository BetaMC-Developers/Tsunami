package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import net.minecraft.server.BlockDispenser;
import net.minecraft.server.TileEntityDispenser;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class CraftDispenser extends CraftBlockState implements Dispenser {
    private final CraftWorld world;
    private final TileEntityDispenser dispenser;

    public CraftDispenser(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        dispenser = (TileEntityDispenser) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public Inventory getInventory() {
        return new CraftInventory(dispenser);
    }

    public boolean dispense() {
        Block block = getBlock();

        synchronized (block) {
            if (block.getType() == Material.DISPENSER) {
                BlockDispenser dispense = (BlockDispenser) net.minecraft.server.Block.DISPENSER;

                dispense.dispense(world.getHandle(), getX(), getY(), getZ(), new Random());
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean update(boolean force) {
        boolean result = super.update(force);

        if (result) {
            dispenser.update();
        }

        return result;
    }

    // Tsunami start
    public void setMetadata(Plugin owningPlugin, String key, MetadataValue value) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");
        Preconditions.checkArgument(value != null, "value cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        dispenser.metadataStore.put(fullKey, value);
    }

    public void removeMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        dispenser.metadataStore.remove(fullKey);
    }

    public MetadataValue getMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        return dispenser.metadataStore.get(fullKey);
    }

    public boolean hasMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        return dispenser.metadataStore.containsKey(fullKey);
    }
    // Tsunami end

}
