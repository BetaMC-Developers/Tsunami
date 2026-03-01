package org.bukkit.metadata;

/**
 * @deprecated This API has been superseded by {@link org.bukkit.persistence}.
 * @see org.bukkit.persistence.PersistentDataType#BYTE
 */
@Deprecated
public class ByteMetadataValue extends MetadataValue {
    public ByteMetadataValue(byte value) {
        super(value);
    }
}
