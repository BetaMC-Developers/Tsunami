package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import net.minecraft.server.TileEntityFurnace;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class CraftFurnace extends CraftBlockState implements Furnace {
    private final CraftWorld world;
    private final TileEntityFurnace furnace;

    public CraftFurnace(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        furnace = (TileEntityFurnace) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public Inventory getInventory() {
        return new CraftInventory(furnace);
    }

    @Override
    public boolean update(boolean force) {
        boolean result = super.update(force);

        if (result) {
            furnace.update();
        }

        return result;
    }

    public short getBurnTime() {
        return (short) furnace.burnTime;
    }

    public void setBurnTime(short burnTime) {
        furnace.burnTime = burnTime;
    }

    public short getCookTime() {
        return (short) furnace.cookTime;
    }

    public void setCookTime(short cookTime) {
        furnace.cookTime = cookTime;
    }

    // Tsunami start
    public void setMetadata(Plugin owningPlugin, String key, MetadataValue value) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");
        Preconditions.checkArgument(value != null, "value cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        furnace.metadataStore.put(fullKey, value);
    }

    public void removeMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        furnace.metadataStore.remove(fullKey);
    }

    public MetadataValue getMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        return furnace.metadataStore.get(fullKey);
    }

    public boolean hasMetadata(Plugin owningPlugin, String key) {
        Preconditions.checkArgument(owningPlugin != null, "owningPlugin cannot be null");
        Preconditions.checkArgument(key != null, "key cannot be null");

        String fullKey = owningPlugin.getDescription().getName().toLowerCase() + "." + key;
        return furnace.metadataStore.containsKey(fullKey);
    }
    // Tsunami end

}
