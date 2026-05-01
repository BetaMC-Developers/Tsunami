package org.bukkit.persistence;

/**
 * Represents an object which is able to store persistent data.
 *
 * @see PersistentDataContainer
 */
public interface PersistentDataHolder {

    /**
     * Returns the {@link PersistentDataContainer} which holds all persistent data
     * stored by this object.
     *
     * @return this object's {@link PersistentDataContainer}
     */
    PersistentDataContainer getPersistentDataContainer();
}
