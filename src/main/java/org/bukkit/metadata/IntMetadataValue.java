package org.bukkit.metadata;

/**
 * @deprecated This API has been superseded by {@link org.bukkit.persistence}.
 * @see org.bukkit.persistence.PersistentDataType#INTEGER
 */
@Deprecated
public class IntMetadataValue extends MetadataValue {
    public IntMetadataValue(int value) {
        super(value);
    }
}
