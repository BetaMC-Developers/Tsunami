package org.bukkit.block;

import org.bukkit.entity.CreatureType;
import org.bukkit.metadata.Metadatable;
import org.bukkit.persistence.PersistentDataHolder;

/**
 * Represents a creature spawner.
 *
 * @author sk89q
 * @author Cogito
 */
public interface CreatureSpawner extends BlockState, PersistentDataHolder, Metadatable { // Tsunami - extends PersistentDataHolder, Metadatable

    /**
     * Get the spawner's creature type.
     *
     * @return
     */
    public CreatureType getCreatureType();

    /**
     * Set the spawner creature type.
     *
     * @param mobType
     */
    public void setCreatureType(CreatureType creatureType);

    /**
     * Get the spawner's creature type.
     *
     * @return
     */
    public String getCreatureTypeId();

    /**
     * Set the spawner mob type.
     *
     * @param creatureType
     */
    public void setCreatureTypeId(String creatureType);

    /**
     * Get the spawner's delay.
     *
     * @return
     */
    public int getDelay();

    /**
     * Set the spawner's delay.
     *
     * @param delay
     */
    public void setDelay(int delay);
}
