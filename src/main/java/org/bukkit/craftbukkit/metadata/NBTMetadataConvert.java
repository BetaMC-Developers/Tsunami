package org.bukkit.craftbukkit.metadata;

import net.minecraft.server.NBTBase;
import net.minecraft.server.NBTTagByte;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagDouble;
import net.minecraft.server.NBTTagFloat;
import net.minecraft.server.NBTTagInt;
import net.minecraft.server.NBTTagLong;
import net.minecraft.server.NBTTagShort;
import net.minecraft.server.NBTTagString;
import org.bukkit.metadata.ByteMetadataValue;
import org.bukkit.metadata.DoubleMetadataValue;
import org.bukkit.metadata.FloatMetadataValue;
import org.bukkit.metadata.IntMetadataValue;
import org.bukkit.metadata.LongMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.ShortMetadataValue;
import org.bukkit.metadata.StringMetadataValue;

import java.util.HashMap;
import java.util.Map;

public class NBTMetadataConvert {

    public static NBTBase metadataToNbt(MetadataValue value) {
        if (value instanceof StringMetadataValue) {
            return new NBTTagString(value.asString());
        } else if (value instanceof ByteMetadataValue) {
            return new NBTTagByte(value.asByte());
        } else if (value instanceof ShortMetadataValue) {
            return new NBTTagShort(value.asShort());
        } else if (value instanceof IntMetadataValue) {
            return new NBTTagInt(value.asInt());
        } else if (value instanceof LongMetadataValue) {
            return new NBTTagLong(value.asLong());
        } else if (value instanceof FloatMetadataValue) {
            return new NBTTagFloat(value.asFloat());
        } else if (value instanceof DoubleMetadataValue) {
            return new NBTTagDouble(value.asDouble());
        }
        return new NBTTagString("");
    }

    public static MetadataValue nbtToMetadata(NBTBase value) {
        if (value instanceof NBTTagString) {
            return new StringMetadataValue(((NBTTagString) value).a);
        } else if (value instanceof NBTTagByte) {
            return new ByteMetadataValue(((NBTTagByte) value).a);
        } else if (value instanceof NBTTagShort) {
            return new ShortMetadataValue(((NBTTagShort) value).a);
        } else if (value instanceof NBTTagInt) {
            return new IntMetadataValue(((NBTTagInt) value).a);
        } else if (value instanceof NBTTagLong) {
            return new LongMetadataValue(((NBTTagLong) value).a);
        } else if (value instanceof NBTTagFloat) {
            return new FloatMetadataValue(((NBTTagFloat) value).a);
        } else if (value instanceof NBTTagDouble) {
            return new DoubleMetadataValue(((NBTTagDouble) value).a);
        }
        return new StringMetadataValue("");
    }

    public static NBTTagCompound metadataToCompound(Map<String, MetadataValue> metadata) {
        NBTTagCompound compound = new NBTTagCompound();
        metadata.forEach((key, value) -> compound.a(key, metadataToNbt(value)));
        return compound;
    }

    public static Map<String, MetadataValue> compoundToMetadata(NBTTagCompound compound) {
        Map<String, MetadataValue> metadata = new HashMap<>();
        compound.a.forEach((key, value) -> metadata.put(String.valueOf(key), nbtToMetadata((NBTBase) value)));
        return metadata;
    }

}
