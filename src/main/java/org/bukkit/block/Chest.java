package org.bukkit.block;

import org.bukkit.metadata.Metadatable;
import org.bukkit.persistence.PersistentDataHolder;

/**
 * Represents a chest.
 *
 * @author sk89q
 */
public interface Chest extends BlockState, ContainerBlock, PersistentDataHolder, Metadatable {} // Tsunami - extends PersistentDataHolder, Metadatable
