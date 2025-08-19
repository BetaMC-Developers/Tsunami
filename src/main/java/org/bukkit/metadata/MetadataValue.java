package org.bukkit.metadata;

/**
 * Represents a metadata value of a {@link Metadatable} object
 */
public abstract class MetadataValue {

    private final Object value;

    protected MetadataValue(Object value) {
        this.value = value;
    }

    /**
     * Gets the raw metadata value
     *
     * @return the metadata value
     */
    public Object value() {
        return value;
    }

    /**
     * Converts the metadata value to a string
     *
     * @return the value as a string
     */
    public String asString() {
        return String.valueOf(value);
    }

    /**
     * Converts the metadata value to a byte
     *
     * @return the value as a byte
     */
    public byte asByte() {
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        return (byte) asInt();
    }

    /**
     * Converts the metadata value to a short
     *
     * @return the value as a short
     */
    public short asShort() {
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        return (short) asInt();
    }

    /**
     * Converts the metadata value to an int
     *
     * @return the value as an int
     */
    public int asInt() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(asString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Converts the metadata value to a long
     *
     * @return the value as a long
     */
    public long asLong() {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(asString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Converts the metadata value to a float
     *
     * @return the value as a float
     */
    public float asFloat() {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return (float) asDouble();
    }

    /**
     * Converts the metadata value to a double
     *
     * @return the value as a double
     */
    public double asDouble() {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(asString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Converts the metadata value to a boolean
     *
     * @return the value as a boolean
     */
    public boolean asBoolean() {
        if (value instanceof Number) {
            return ((Number) value).byteValue() != 0;
        }
        return Boolean.parseBoolean(asString());
    }

}
