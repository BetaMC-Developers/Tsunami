package org.bukkit.persistence;

import java.util.Set;

public interface PersistentDataContainer {

    <P, C> void set(String key, PersistentDataType<P, C> type, C value);

    void remove(String key);

    <P, C> boolean has(String key, PersistentDataType<P, C> type);

    boolean has(String key);

    <P, C> C get(String key, PersistentDataType<P, C> type);

    <P, C> C getOrDefault(String key, PersistentDataType<P, C> type, C defaultValue);

    Set<String> getKeys();

    boolean isEmpty();

    void copyTo(PersistentDataContainer other, boolean replace);
}
