package org.bukkit.persistence;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

public interface ListPersistentDataType<P, C> extends PersistentDataType<List<P>, List<C>> {

    ListPersistentDataType<Byte, Byte> BYTE = listTypeFrom(PersistentDataType.BYTE);
    ListPersistentDataType<Short, Short> SHORT = listTypeFrom(PersistentDataType.SHORT);
    ListPersistentDataType<Integer, Integer> INTEGER = listTypeFrom(PersistentDataType.INTEGER);
    ListPersistentDataType<Long, Long> LONG = listTypeFrom(PersistentDataType.LONG);
    ListPersistentDataType<Float, Float> FLOAT = listTypeFrom(PersistentDataType.FLOAT);
    ListPersistentDataType<Double, Double> DOUBLE = listTypeFrom(PersistentDataType.DOUBLE);
    ListPersistentDataType<Byte, Boolean> BOOLEAN = listTypeFrom(PersistentDataType.BOOLEAN);
    ListPersistentDataType<Short, Character> CHARACTER = listTypeFrom(PersistentDataType.CHARACTER);
    ListPersistentDataType<String, String> STRING = listTypeFrom(PersistentDataType.STRING);
    ListPersistentDataType<byte[], byte[]> BYTE_ARRAY = listTypeFrom(PersistentDataType.BYTE_ARRAY);
    ListPersistentDataType<PersistentDataContainer, PersistentDataContainer> DATA_CONTAINER = listTypeFrom(PersistentDataType.DATA_CONTAINER);

    PersistentDataType<P, C> getElementType();

    static <P, C> ListPersistentDataType<P, C> listTypeFrom(PersistentDataType<P, C> type) {
        return new ListPersistentDataTypeImpl<>(type);
    }

    final class ListPersistentDataTypeImpl<P, C> implements ListPersistentDataType<P, C> {
        private final PersistentDataType<P, C> elementType;

        ListPersistentDataTypeImpl(PersistentDataType<P, C> elementType) {
            this.elementType = elementType;
        }

        @Override
        public Class<List<P>> getPrimitiveType() {
            return (Class<List<P>>) (Object) List.class;
        }

        @Override
        public Class<List<C>> getComplexType() {
            return (Class<List<C>>) (Object) List.class;
        }

        @Override
        public List<P> toPrimitive(List<C> complex) {
            return complex.stream().map(this.elementType::toPrimitive).collect(Collectors.toList());
        }

        @Override
        public List<C> fromPrimitive(List<P> primitive) {
            return Lists.transform(primitive, this.elementType::fromPrimitive);
        }

        @Override
        public PersistentDataType<P, C> getElementType() {
            return this.elementType;
        }
    }

}
