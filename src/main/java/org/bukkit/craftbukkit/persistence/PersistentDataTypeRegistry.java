package org.bukkit.craftbukkit.persistence;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.server.NBTBase;
import net.minecraft.server.NBTTagByte;
import net.minecraft.server.NBTTagByteArray;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagDouble;
import net.minecraft.server.NBTTagFloat;
import net.minecraft.server.NBTTagInt;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagLong;
import net.minecraft.server.NBTTagShort;
import net.minecraft.server.NBTTagString;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PersistentDataTypeRegistry {

    private final Map<PersistentDataType<?, ?>, PrimitiveToTagAdapter<?, ?>> adapters = new HashMap<>();

    public synchronized <P, T extends NBTBase> PrimitiveToTagAdapter<P, T> getOrCreateAdapter(PersistentDataType<P, ?> type) {
        PrimitiveToTagAdapter<P, T> adapter = (PrimitiveToTagAdapter<P, T>) this.adapters.get(type);
        if (adapter == null) {
            adapter = createAdapter(type);
            this.adapters.put(type, adapter);
        }
        return adapter;
    }

    private <P> PrimitiveToTagAdapter createAdapter(PersistentDataType<P, ?> type) {
        if (Byte.class.equals(type.getPrimitiveType())) {
            return new PrimitiveToTagAdapter<Byte, NBTTagByte>(
                    NBTTagByte::new, tag -> tag.a, tag -> tag instanceof NBTTagByte
            );
        } else if (Short.class.equals(type.getPrimitiveType())) {
            return new PrimitiveToTagAdapter<Short, NBTTagShort>(
                    NBTTagShort::new, tag -> tag.a, tag -> tag instanceof NBTTagShort
            );
        } else if (Integer.class.equals(type.getPrimitiveType())) {
            return new PrimitiveToTagAdapter<Integer, NBTTagInt>(
                    NBTTagInt::new, tag -> tag.a, tag -> tag instanceof NBTTagInt
            );
        } else if (Long.class.equals(type.getPrimitiveType())) {
            return new PrimitiveToTagAdapter<Long, NBTTagLong>(
                    NBTTagLong::new, tag -> tag.a, tag -> tag instanceof NBTTagLong
            );
        } else if (Float.class.equals(type.getPrimitiveType())) {
            return new PrimitiveToTagAdapter<Float, NBTTagFloat>(
                    NBTTagFloat::new, tag -> tag.a, tag -> tag instanceof NBTTagFloat
            );
        } else if (Double.class.equals(type.getPrimitiveType())) {
            return new PrimitiveToTagAdapter<Double, NBTTagDouble>(
                    NBTTagDouble::new, tag -> tag.a, tag -> tag instanceof NBTTagDouble
            );
        } else if (String.class.equals(type.getPrimitiveType())) {
            return new PrimitiveToTagAdapter<String, NBTTagString>(
                    NBTTagString::new, tag -> tag.a, tag -> tag instanceof NBTTagString
            );
        } else if (byte[].class.equals(type.getPrimitiveType())) {
            return new PrimitiveToTagAdapter<byte[], NBTTagByteArray>(
                    NBTTagByteArray::new, tag -> tag.a, tag -> tag instanceof NBTTagByteArray
            );
        } else if (PersistentDataContainer.class.equals(type.getPrimitiveType())) {
            return new PrimitiveToTagAdapter<CraftPersistentDataContainer, NBTTagCompound>(
                    CraftPersistentDataContainer::asCompound, CraftPersistentDataContainer::new, tag -> tag instanceof NBTTagCompound
            );
        } else if (List.class.equals(type.getPrimitiveType())) {
            Preconditions.checkArgument(type instanceof ListPersistentDataType, "type must be a ListPersistentDataType");
            ListPersistentDataType<?, ?> listType = (ListPersistentDataType<?, ?>) type;
            PrimitiveToTagAdapter elementAdapter = getOrCreateAdapter(listType.getElementType());

            return new PrimitiveToTagAdapter<List, NBTTagList>(
                    list -> {
                        NBTTagList tag = new NBTTagList();
                        list.forEach(p -> tag.a(elementAdapter.serialize(p)));
                        return tag;
                    },
                    tag -> Lists.transform(tag.a, e -> {
                        Preconditions.checkState(elementAdapter.matches((NBTBase) e));
                        return elementAdapter.deserialize((NBTBase) e);
                    }),
                    tag -> tag instanceof NBTTagList
            );
        } else {
            throw new IllegalArgumentException("illegal primitive type " + type.getClass().getName());
        }
    }

}
