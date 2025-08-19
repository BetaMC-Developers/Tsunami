package org.bukkit.block;

import org.bukkit.metadata.Metadatable;

/**
 * Represents a furnace.
 *
 * @author sk89q
 */
public interface Furnace extends BlockState, ContainerBlock, Metadatable { // Tsunami - extends Metadatable

    /**
     * Get burn time.
     *
     * @return
     */
    public short getBurnTime();

    /**
     * Set burn time.
     *
     * @param burnTime
     */
    public void setBurnTime(short burnTime);

    /**
     * Get cook time.
     *
     * @return
     */
    public short getCookTime();

    /**
     * Set cook time.
     *
     * @param cookTime
     */
    public void setCookTime(short cookTime);
}
