package org.bukkit.event.world;

import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Called when a tree grows in the world, naturally or when using bonemeal.
 * <p>
 * If this event is cancelled, the tree will not grow.
 */
public class TreeGrowEvent extends WorldEvent implements Cancellable {

    private final Location location;
    private final TreeType species;
    private final @Nullable Player player;
    private final List<BlockState> blocks;

    private boolean cancelled;

    public TreeGrowEvent(Location location, TreeType species, @Nullable Player player, List<BlockState> blocks) {
        super(Type.TREE_GROW, location.getWorld());
        this.location = location;
        this.species = species;
        this.player = player;
        this.blocks = blocks;
    }

    /**
     * Gets the location where the tree will grow.
     *
     * @return the location where the tree will grow
     */
    public Location getLocation() {
        return this.location.clone();
    }

    /**
     * Gets the tree species.
     *
     * @return the tree species
     */
    public TreeType getSpecies() {
        return this.species;
    }

    /**
     * Gets if the tree was grown using bonemeal.
     *
     * @return {@code true} if the tree was grown using bonemeal
     */
    public boolean isFromBonemeal() {
        return this.player != null;
    }

    /**
     * Gets the player who used bonemeal to grow the tree.
     *
     * @return the player who used bonemeal, or {@code null} if the tree grew
     *         naturally
     */
    public @Nullable Player getPlayer() {
        return this.player;
    }

    /**
     * Gets the blocks which make up the tree.
     *
     * @return a list of blocks which make up the tree
     */
    public List<BlockState> getBlocks() {
        return this.blocks;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
