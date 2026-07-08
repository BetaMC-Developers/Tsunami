package org.bukkit.event.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Cancellable;

/**
 * Called when a block grows naturally in the world.
 * <p>
 * This applies to:
 * <ul>
 *     <li>Wheat</li>
 *     <li>Sugar Cane</li>
 *     <li>Cactus</li>
 * </ul>
 * <p>
 * If this event is cancelled, the block will not grow.
 */
public class BlockGrowEvent extends BlockEvent implements Cancellable {

    private final BlockState newState;
    private boolean cancelled;

    public BlockGrowEvent(Block block, BlockState newState) {
        super(Type.BLOCK_GROW, block);
        this.newState = newState;
    }

    /**
     * Gets the state of the block where it will grow to.
     *
     * @return the block state for this event's block
     */
    public BlockState getNewState() {
        return this.newState;
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
