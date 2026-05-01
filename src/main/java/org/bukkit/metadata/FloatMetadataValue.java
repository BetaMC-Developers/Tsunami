package org.bukkit.metadata;

/**
 * @deprecated This API has been superseded by {@link org.bukkit.persistence}.
 * @see org.bukkit.persistence.PersistentDataType#FLOAT
 */
@Deprecated
public class FloatMetadataValue extends MetadataValue {
    public FloatMetadataValue(float value) {
        super(value);
    }
}
