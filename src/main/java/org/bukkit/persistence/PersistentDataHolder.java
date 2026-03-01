package org.bukkit.persistence;

public interface PersistentDataHolder {

    PersistentDataContainer getPersistentDataContainer();

    PersistentDataContainer newPersistentDataContainer();
}
