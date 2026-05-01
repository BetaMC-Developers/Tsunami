package org.bukkit.persistence;

/**
 * Represents a data type which is used to convert a complex value to a
 * primitive value, and vice versa. This is used by {@link PersistentDataContainer}
 * for storage and retrieval of values.
 * <p>
 * Allowed primitive types are {@code Byte}, {@code Short}, {@code Integer},
 * {@code Long}, {@code Float}, {@code Double}, {@code String}, {@code byte[]}
 * and {@code PersistentDataContainer}.
 *
 * @see PersistentDataContainer
 * @param <P> the primitive type
 * @param <C> the complex type
 */
public interface PersistentDataType<P, C> {

    PersistentDataType<Byte, Byte> BYTE = new PrimitivePersistentDataType<>(Byte.class);
    PersistentDataType<Short, Short> SHORT = new PrimitivePersistentDataType<>(Short.class);
    PersistentDataType<Integer, Integer> INTEGER = new PrimitivePersistentDataType<>(Integer.class);
    PersistentDataType<Long, Long> LONG = new PrimitivePersistentDataType<>(Long.class);
    PersistentDataType<Float, Float> FLOAT = new PrimitivePersistentDataType<>(Float.class);
    PersistentDataType<Double, Double> DOUBLE = new PrimitivePersistentDataType<>(Double.class);
    PersistentDataType<Byte, Boolean> BOOLEAN = new BooleanPersistentDataType();
    PersistentDataType<Short, Character> CHARACTER = new CharacterPersistentDataType();
    PersistentDataType<String, String> STRING = new PrimitivePersistentDataType<>(String.class);
    PersistentDataType<byte[], byte[]> BYTE_ARRAY = new PrimitivePersistentDataType<>(byte[].class);
    PersistentDataType<PersistentDataContainer, PersistentDataContainer> DATA_CONTAINER = new PrimitivePersistentDataType<>(PersistentDataContainer.class);

    /**
     * Returns the primitive type of a value of this data type.
     *
     * @return the primitive type
     */
    Class<P> getPrimitiveType();

    /**
     * Returns the complex type of a value of this data type.
     *
     * @return the complex type
     */
    Class<C> getComplexType();

    /**
     * Converts the given complex value to a primitive value.
     *
     * @param complex the complex value
     * @return the primitive value
     */
    P toPrimitive(C complex);

    /**
     * Converts the given primitive value to a complex value.
     *
     * @param primitive the primitive value
     * @return the complex value
     */
    C fromPrimitive(P primitive);

    final class PrimitivePersistentDataType<P> implements PersistentDataType<P, P> {
        private final Class<P> primitiveType;

        PrimitivePersistentDataType(Class<P> primitiveType) {
            this.primitiveType = primitiveType;
        }

        @Override
        public Class<P> getPrimitiveType() {
            return this.primitiveType;
        }

        @Override
        public Class<P> getComplexType() {
            return this.primitiveType;
        }

        @Override
        public P toPrimitive(P complex) {
            return complex;
        }

        @Override
        public P fromPrimitive(P primitive) {
            return primitive;
        }
    }

    final class BooleanPersistentDataType implements PersistentDataType<Byte, Boolean> {

        @Override
        public Class<Byte> getPrimitiveType() {
            return Byte.class;
        }

        @Override
        public Class<Boolean> getComplexType() {
            return Boolean.class;
        }

        @Override
        public Byte toPrimitive(Boolean complex) {
            return (byte) (complex ? 1 : 0);
        }

        @Override
        public Boolean fromPrimitive(Byte primitive) {
            return primitive != 0;
        }
    }

    final class CharacterPersistentDataType implements PersistentDataType<Short, Character> {

        @Override
        public Class<Short> getPrimitiveType() {
            return Short.class;
        }

        @Override
        public Class<Character> getComplexType() {
            return Character.class;
        }

        @Override
        public Short toPrimitive(Character complex) {
            return (short) complex.charValue();
        }

        @Override
        public Character fromPrimitive(Short primitive) {
            return (char) primitive.shortValue();
        }
    }

}
