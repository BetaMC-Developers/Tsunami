package org.betamc.tsunami.rcon;

import java.nio.charset.StandardCharsets;

public class RconUtil {

    public static String stringFromByteArray(byte[] input, int offset, int length) {
        int i = length - 1;
        int i1 = offset > i ? i : offset;

        while (0 != input[i1] && i1 < i) {
            i1++;
        }

        return new String(input, offset, i1 - offset, StandardCharsets.UTF_8);
    }

    public static int intFromByteArray(byte[] input, int offset) {
        return intFromByteArray(input, offset, input.length);
    }

    public static int intFromByteArray(byte[] input, int offset, int length) {
        return 0 > length - offset - 4
                ? 0
                : input[offset + 3] << 24 | (input[offset + 2] & 0xFF) << 16 | (input[offset + 1] & 0xFF) << 8 | input[offset] & 0xFF;
    }

}
