package org.bukkit.craftbukkit.persistence;

import net.minecraft.server.NBTBase;
import net.minecraft.server.NBTTagCompound;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CraftPersistentDataContainer implements PersistentDataContainer {

    public static final String TAG_KEY = "PersistentDataContainer";
    private static final PersistentDataTypeRegistry REGISTRY = new PersistentDataTypeRegistry();

    private final NBTTagCompound compound;

    public CraftPersistentDataContainer() {
        this.compound = new NBTTagCompound();
    }

    public CraftPersistentDataContainer(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public <P, C> void set(String key, PersistentDataType<P, C> type, C value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(value, "value must not be null");

        NBTBase tag = REGISTRY.getOrCreateAdapter(type).serialize(type.toPrimitive(value));
        this.compound.a(key, tag);
    }

    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, "key must not be null");

        this.compound.a.remove(key);
    }

    @Override
    public <P, C> boolean has(String key, PersistentDataType<P, C> type) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(type, "type must not be null");

        NBTBase tag = (NBTBase) this.compound.a.get(key);
        return tag != null && REGISTRY.getOrCreateAdapter(type).matches(tag);
    }

    @Override
    public boolean has(String key) {
        Objects.requireNonNull(key, "key must not be null");

        return this.compound.a.containsKey(key);
    }

    @Override
    public <P, C> C get(String key, PersistentDataType<P, C> type) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(type, "type must not be null");

        NBTBase tag = (NBTBase) this.compound.a.get(key);
        if (tag == null) {
            return null;
        }
        P primitive = REGISTRY.getOrCreateAdapter(type).deserialize(tag);
        return type.fromPrimitive(primitive);
    }

    @Override
    public <P, C> C getOrDefault(String key, PersistentDataType<P, C> type, C defaultValue) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(defaultValue, "defaultValue must not be null");

        C value = get(key, type);
        return value != null ? value : defaultValue;
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(new HashSet<>(this.compound.a.keySet()));
    }

    @Override
    public boolean isEmpty() {
        return this.compound.a.isEmpty();
    }

    @Override
    public void copyTo(PersistentDataContainer other, boolean replace) {
        Objects.requireNonNull(other, "other must not be null");

        CraftPersistentDataContainer target = (CraftPersistentDataContainer) other;
        if (replace) {
            target.asCompound().a.putAll(this.compound.a);
        } else {
            this.compound.a.forEach(target.asCompound().a::putIfAbsent);
        }
    }

    public NBTTagCompound asCompound() {
        return this.compound;
    }

}
