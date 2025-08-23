package org.bukkit.craftbukkit;

import java.util.HashMap;
import java.util.Map;

public enum ChunkCompressionType {

    GZIP("gzip", 1),
    DEFLATE("deflate", 2),
    LZ4("lz4", 3);

    private static final Map<String, ChunkCompressionType> byName = new HashMap<>();
    private final String name;
    private final int id;

    ChunkCompressionType(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public static ChunkCompressionType fromName(String name) {
        return byName.get(name);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    static {
        for (ChunkCompressionType type : values()) {
            byName.put(type.name, type);
        }
    }

}
