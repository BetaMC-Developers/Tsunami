package org.bukkit.craftbukkit;

import org.betamc.tsunami.Tsunami;

import java.util.regex.Pattern;

public class TextWrapper {
    private static final int[] characterWidths = new int[] {
        1, 9, 9, 8, 8, 8, 8, 7, 9, 8, 9, 9, 8, 9, 9, 9,
        8, 8, 8, 8, 9, 9, 8, 9, 8, 8, 8, 8, 8, 9, 9, 9,
        4, 2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6,
        7, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6,
        3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6,
        6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 5, 2, 5, 7, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 3, 6, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6,
        6, 3, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 2, 6, 6,
        8, 9, 9, 6, 6, 6, 8, 8, 6, 8, 8, 8, 8, 8, 6, 6,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9, 9, 9, 5, 9, 9,
        8, 7, 7, 8, 7, 8, 8, 8, 7, 8, 8, 7, 9, 9, 6, 7,
        7, 7, 7, 7, 9, 6, 7, 8, 7, 6, 6, 9, 7, 6, 7, 1
    };
    public static final char COLOR_CHAR = '\u00A7';
    public static final Pattern COLOR_PATTERN = Pattern.compile(COLOR_CHAR + "[0-9a-fA-F]"); // Tsunami
    public static final int CHAT_WINDOW_WIDTH = 320;
    public static final int CHAT_STRING_LENGTH = 119;
    public static final String allowedChars = net.minecraft.server.FontAllowedCharacters.allowedCharacters;

    public static String[] wrapText(final String input) {
        // Tsunami start
        final String text;

        if (Tsunami.config().getBoolean("sanitize-text", false)) {
            text = sanitizeText(input);
        }
        else {
            text = input;
        }
        // Tsunami end

        final StringBuilder out = new StringBuilder();
        char colorChar = 'f';
        int lineWidth = 0;
        int lineLength = 0;

        // Go over the message char by char.
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            // Get the color
            if (ch == COLOR_CHAR && i < text.length() - 1) {
                // We might need a linebreak ... so ugly ;(
                if (lineLength + 2 > CHAT_STRING_LENGTH) {
                    out.append('\n');
                    lineLength = 0;
                    if (colorChar != 'f' && colorChar != 'F') {
                        out.append(COLOR_CHAR).append(colorChar);
                        lineLength += 2;
                    }
                }
                colorChar = text.charAt(++i);
                out.append(COLOR_CHAR).append(colorChar);
                lineLength += 2;
                continue;
            }

            // Figure out if it's allowed
            int index = allowedChars.indexOf(ch);
            if (index == -1) {
                // Invalid character .. skip it.
                continue;
            } else {
                // Sadly needed as the allowedChars string misses the first
                index += 32;
            }

            // Find the width
            final int width = characterWidths[index];

            // See if we need a linebreak
            if (lineLength + 1 > CHAT_STRING_LENGTH || lineWidth + width >= CHAT_WINDOW_WIDTH) {
                out.append('\n');
                lineLength = 0;

                // Re-apply the last color if it isn't the default
                if (colorChar != 'f' && colorChar != 'F') {
                    out.append(COLOR_CHAR).append(colorChar);
                    lineLength += 2;
                }
                lineWidth = width;
            } else {
                lineWidth += width;
            }
            out.append(ch);
            lineLength++;
        }

        // Return it split
        return out.toString().split("\n");
    }

    // Tsunami start
    public static String sanitizeText(final String input) {
        String text = trimTrailing(input);

        // Remove all trailing whitespaces and color codes
        while (endsWithColor(text)) {
            text = trimTrailing(text.substring(0, text.length() - 2));
        }

        StringBuilder sb = new StringBuilder();
        char prevColor = 'f';
        char currentColor = 'f';

        // Filter out all redundant color codes
        for (int i = 0; i < text.length(); i++) {
            // If there are multiple color codes chained together, we will get the last one
            while (colorAt(text, i)) {
                currentColor = text.charAt(++i);
                i++;
            }

            // If a new color was found, place it at the beginning of the next word
            if (Character.toLowerCase(prevColor) != Character.toLowerCase(currentColor)
                && !Character.isWhitespace(text.charAt(i))) {

                sb.append(COLOR_CHAR).append(currentColor);
                prevColor = currentColor;
            }

            sb.append(text.charAt(i));
        }

        text = sb.toString();
        sb.setLength(0);

        // Remove all illegal characters
        for (int i = 0; i < text.length(); i ++) {
            char ch = text.charAt(i);
            if (allowedChars.indexOf(ch) != -1 || colorAt(text, i)) {
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    private static String trimTrailing(final String input) {
        int length = input.length();
        while (length > 0 && Character.isWhitespace(input.charAt(length - 1))) {
            length--;
        }
        return input.substring(0, length);
    }

    private static boolean colorAt(final String input, int index) {
        if (index < 0 || index > input.length() - 2) return false;
        return COLOR_PATTERN.matcher(input.substring(index, index + 2)).matches();
    }

    private static boolean endsWithColor(final String input) {
        return input.length() >= 2 && COLOR_PATTERN.matcher(input.substring(input.length() - 2)).matches();
    }
    // Tsunami end

    /**
     * Calculates the width of a string in pixels based on Minecraft's character widths.
     * The maximum width for chat is 320 pixels (Use CHAT_WINDOW_WIDTH).
     *
     * @param text The input string.
     * @return The width of the string in pixels.
     */
    public static int widthInPixels(final String text) {
        if (text == null || text.isEmpty())
            return 0;

        int output = 0;

        // literally yoinked from above and removed unnecessary components.
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch == COLOR_CHAR && i < text.length() - 1) {
                i++;
                continue;
            }

            int index = allowedChars.indexOf(ch);
            if (index == -1)
                continue;

            index += 32; // compensate for gap in allowed characters

            output += characterWidths[index];
        }

        return output;
    }
}
