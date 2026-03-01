package org.bukkit.persistence;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a data type which is used to convert a list of complex values
 * to a list of primitive values, and vice versa. This is used by {@link PersistentDataContainer}
 * for storage and retrieval of values of type {@link List}.
 * <p>
 * Allowed primitive types are {@code Byte}, {@code Short}, {@code Integer},
 * {@code Long}, {@code Float}, {@code Double}, {@code String}, {@code byte[]}
 * and {@code PersistentDataContainer}.
 *
 * @see PersistentDataContainer
 * @param <P> the primitive element type
 * @param <C> the complex element type
 */
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

    /**
     * Returns the data type which elements of a list of this type conform to.
     *
     * @return the element data type
     */
    PersistentDataType<P, C> getElementType();

    /**
     * Creates a {@link ListPersistentDataType} from the specified element data type.
     *
     * @param type the element data type
     * @return a new list data type
     */
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
