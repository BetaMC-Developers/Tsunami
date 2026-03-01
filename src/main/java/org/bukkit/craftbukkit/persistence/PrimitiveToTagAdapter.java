package org.bukkit.craftbukkit.persistence;

import net.minecraft.server.NBTBase;

import java.util.function.Function;
import java.util.function.Predicate;

public class PrimitiveToTagAdapter<P, T extends NBTBase> {

    private final Function<P, T> serializer;
    private final Function<T, P> deserializer;
    private final Predicate<NBTBase> matcher;

    public PrimitiveToTagAdapter(Function<P, T> serializer, Function<T, P> deserializer, Predicate<NBTBase> matcher) {
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.matcher = matcher;
    }

    public T serialize(P primitive) {
        return this.serializer.apply(primitive);
    }

    public P deserialize(T tag) {
        return this.deserializer.apply(tag);
    }

    public boolean matches(T tag) {
        return this.matcher.test(tag);
    }

}
