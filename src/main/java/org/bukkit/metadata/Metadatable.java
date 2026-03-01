package org.bukkit.metadata;

import org.bukkit.plugin.Plugin;

/**
 * Represents an object that can provide metadata about itself
 *
 * @deprecated This API has been superseded by {@link org.bukkit.persistence}.
 * @see org.bukkit.persistence.PersistentDataContainer
 */
@Deprecated
public interface Metadatable {

    /**
     * Sets a metadata value for this object
     *
     * @param owningPlugin the plugin owning the metadata
     * @param key the unique identifier for the metadata
     * @param value the metadata value
     * @deprecated use {@link org.bukkit.persistence.PersistentDataContainer#set(String, org.bukkit.persistence.PersistentDataType, Object)}
     */
    @Deprecated
    void setMetadata(Plugin owningPlugin, String key, MetadataValue value);

    /**
     * Removes a metadata value from this object
     *
     * @param owningPlugin the plugin owning the metadata
     * @param key the unique identifier for the metadata
     * @deprecated use {@link org.bukkit.persistence.PersistentDataContainer#remove(String)}
     */
    @Deprecated
    void removeMetadata(Plugin owningPlugin, String key);

    /**
     * Gets a metadata value from this object
     *
     * @param owningPlugin the plugin owning the metadata
     * @param key the unique identifier for the metadata
     * @return the metadata value, or null if it does not exist
     * @deprecated use {@link org.bukkit.persistence.PersistentDataContainer#get(String, org.bukkit.persistence.PersistentDataType)}
     */
    @Deprecated
    MetadataValue getMetadata(Plugin owningPlugin, String key);

    /**
     * Tests whether this object has a metadata value
     *
     * @param owningPlugin the plugin owning the metadata
     * @param key the unique identifier for the metadata
     * @return true if the metadata exists, false if not
     * @deprecated use {@link org.bukkit.persistence.PersistentDataContainer#has(String)}
     */
    @Deprecated
    boolean hasMetadata(Plugin owningPlugin, String key);

}
