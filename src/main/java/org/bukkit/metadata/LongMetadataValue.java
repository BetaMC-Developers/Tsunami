package org.bukkit.metadata;

/**
 * @deprecated This API has been superseded by {@link org.bukkit.persistence}.
 * @see org.bukkit.persistence.PersistentDataType#LONG
 */
@Deprecated
public class LongMetadataValue extends MetadataValue {
    public LongMetadataValue(long value) {
        super(value);
    }
}
