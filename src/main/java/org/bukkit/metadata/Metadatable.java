package org.bukkit.metadata;

import org.bukkit.plugin.Plugin;

/**
 * Represents an object that can provide metadata about itself
 */
public interface Metadatable {

    /**
     * Sets a metadata value for this object
     *
     * @param owningPlugin the plugin owning the metadata
     * @param key the unique identifier for the metadata
     * @param value the metadata value
     */
    void setMetadata(Plugin owningPlugin, String key, MetadataValue value);

    /**
     * Removes a metadata value from this object
     *
     * @param owningPlugin the plugin owning the metadata
     * @param key the unique identifier for the metadata
     */
    void removeMetadata(Plugin owningPlugin, String key);

    /**
     * Gets a metadata value from this object
     *
     * @param owningPlugin the plugin owning the metadata
     * @param key the unique identifier for the metadata
     * @return the metadata value, or null if it does not exist
     */
    MetadataValue getMetadata(Plugin owningPlugin, String key);

    /**
     * Tests whether this object has a metadata value
     *
     * @param owningPlugin the plugin owning the metadata
     * @param key the unique identifier for the metadata
     * @return true if the metadata exists, false if not
     */
    boolean hasMetadata(Plugin owningPlugin, String key);

}
