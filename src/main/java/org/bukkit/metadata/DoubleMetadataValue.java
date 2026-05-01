package org.bukkit.metadata;

/**
 * @deprecated This API has been superseded by {@link org.bukkit.persistence}.
 * @see org.bukkit.persistence.PersistentDataType#DOUBLE
 */
@Deprecated
public class DoubleMetadataValue extends MetadataValue {
    public DoubleMetadataValue(double value) {
        super(value);
    }
}
