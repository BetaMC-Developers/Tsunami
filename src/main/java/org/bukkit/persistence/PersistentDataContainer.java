package org.bukkit.persistence;

import java.util.Set;

/**
 * Represents a container which stores persistent data using key-value mappings.
 * <p>
 * A container can store simple values, but it can also store lists of values
 * and even other containers, enabling complex and nested data structures
 * to be stored.
 * <p>
 * A distinction is made between primitive and complex types: when storing a
 * value, a {@link PersistentDataType} must be provided in order to convert
 * the complex value into a primitive value. When retrieving a value,
 * the primitive value is converted back to a complex value.
 *
 * @see PersistentDataType
 */
public interface PersistentDataContainer {

    /**
     * Stores a new key-value mapping in this container, or replaces the value
     * if a mapping with the specified key is already present.
     *
     * @param key the unique key of the mapping
     * @param type the {@link PersistentDataType} used to convert
     *             the complex value to a primitive value
     * @param value the complex value
     */
    <P, C> void set(String key, PersistentDataType<P, C> type, C value);

    /**
     * Removes a key-value mapping from this container if it is present.
     *
     * @param key the unique key of the mapping to remove
     */
    void remove(String key);

    /**
     * Tests if a key-value mapping with the specified key and a value conforming
     * to the {@link PersistentDataType} is present in this container.
     *
     * @param key the unique key of the mapping to test for
     * @param type the {@link PersistentDataType} which the value should conform to
     * @return {@code true} if a key-value mapping with this key and whose value
     *         conforms to the type exists
     */
    <P, C> boolean has(String key, PersistentDataType<P, C> type);

    /**
     * Tests if a key-value mapping with the specified key is present in this container.
     *
     * @param key the unique key of the mapping to test for
     * @return {@code true} if a key-value mapping with this key exists
     */
    boolean has(String key);

    /**
     * Retrieves a value from a key-value mapping present in this container.
     *
     * @param key the unique key of the mapping
     * @param type the {@link PersistentDataType} used to convert
     *             the primitive value to a complex value
     * @return the value associated with this key, or {@code null}
     *         if no mapping with this key exists
     */
    <P, C> C get(String key, PersistentDataType<P, C> type);

    /**
     * Retrieves a value from a key-value mapping present in this container,
     * or returns the specified default value if no such mapping exists.
     *
     * @param key the unique key of the mapping
     * @param type the {@link PersistentDataType} used to convert
     *             the primitive value to a complex value
     * @param defaultValue the value to return if the mapping is not present
     * @return the value associated with this key, or {@code defaultValue}
     *         if no mapping with this key exists
     */
    <P, C> C getOrDefault(String key, PersistentDataType<P, C> type, C defaultValue);

    /**
     * Returns a copy of the keys of all mappings present in this container.
     *
     * @return the keys of all mappings in this container
     */
    Set<String> getKeys();

    /**
     * Tests if this container holds no key-value mappings.
     *
     * @return {@code true} if no mappings are present in this container
     */
    boolean isEmpty();

    /**
     * Copies all key-value mappings present in this container to another container.
     *
     * @param other the container to copy this container's mappings to
     * @param replace if mappings from this container should replace mappings
     *                which are already present in the other container
     */
    void copyTo(PersistentDataContainer other, boolean replace);
}
