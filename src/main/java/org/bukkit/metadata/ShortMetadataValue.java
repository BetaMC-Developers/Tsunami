package org.bukkit.metadata;

/**
 * @deprecated This API has been superseded by {@link org.bukkit.persistence}.
 * @see org.bukkit.persistence.PersistentDataType#SHORT
 */
@Deprecated
public class ShortMetadataValue extends MetadataValue {
    public ShortMetadataValue(short value) {
        super(value);
    }
}
