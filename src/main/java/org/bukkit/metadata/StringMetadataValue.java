package org.bukkit.metadata;

/**
 * @deprecated This API has been superseded by {@link org.bukkit.persistence}.
 * @see org.bukkit.persistence.PersistentDataType#STRING
 */
@Deprecated
public class StringMetadataValue extends MetadataValue {
    public StringMetadataValue(String value) {
        super(value);
    }
}
