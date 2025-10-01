package org.bukkit.craftbukkit.util;

/**
 *
 * @author Nathan
 */
public abstract class LongHash {
    // Tsunami - package-private -> public
    public static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + lsw - Integer.MIN_VALUE;
    }

    // Tsunami - package-private -> public
    public static int msw(long l) {
        return (int) (l >> 32);
    }

    // Tsunami - package-private -> public
    public static int lsw(long l) {
        return (int) (l & 0xFFFFFFFF) + Integer.MIN_VALUE;
    }

    // Tsunami start
    public static long toLong(int high, int mid, int low) {
        long h = ((long) high & 0x0FFFFFFFL) << 36;
        long m = ((long) mid & 0xFFL) << 28;
        long l = ((long) low & 0x0FFFFFFFL);
        return h | m | l;
    }

    public static int high(long l) {
        int i = (int) ((l >>> 36) & 0x0FFFFFFFL);
        if ((i & (1L << 27)) != 0)
            i |= ~0x0FFFFFFF;
        return i;
    }

    public static int mid(long l) {
        return (byte) ((l >>> 28) & 0xFFL);
    }

    public static int low(long l) {
        int i = (int) (l & 0x0FFFFFFFL);
        if ((i & (1 << 27)) != 0)
            i |= ~0x0FFFFFFF;
        return i;
    }
    // Tsunami end

    public boolean containsKey(int msw, int lsw) {
        return containsKey(toLong(msw, lsw));
    }

    public void remove(int msw, int lsw) {
        remove(toLong(msw, lsw));
    }

    public abstract boolean containsKey(long key);

    public abstract void remove(long key);
}
