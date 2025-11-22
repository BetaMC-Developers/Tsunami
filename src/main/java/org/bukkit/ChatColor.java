package org.bukkit;

import net.kyori.ansi.ANSIComponentRenderer;
import net.kyori.ansi.StyleOps;

import java.util.HashMap;
import java.util.Map;

/**
 * All supported color values for chat
 */
public enum ChatColor {

    /**
     * Represents black
     */
    BLACK(0x0),
    /**
     * Represents dark blue
     */
    DARK_BLUE(0x1),
    /**
     * Represents dark green
     */
    DARK_GREEN(0x2),
    /**
     * Represents dark blue (aqua)
     */
    DARK_AQUA(0x3),
    /**
     * Represents dark red
     */
    DARK_RED(0x4),
    /**
     * Represents dark purple
     */
    DARK_PURPLE(0x5),
    /**
     * Represents gold
     */
    GOLD(0x6),
    /**
     * Represents gray
     */
    GRAY(0x7),
    /**
     * Represents dark gray
     */
    DARK_GRAY(0x8),
    /**
     * Represents blue
     */
    BLUE(0x9),
    /**
     * Represents green
     */
    GREEN(0xA),
    /**
     * Represents aqua
     */
    AQUA(0xB),
    /**
     * Represents red
     */
    RED(0xC),
    /**
     * Represents light purple
     */
    LIGHT_PURPLE(0xD),
    /**
     * Represents yellow
     */
    YELLOW(0xE),
    /**
     * Represents white
     */
    WHITE(0xF);

    private final int code;
    private final static Map<Integer, ChatColor> colors = new HashMap<Integer, ChatColor>();

    private ChatColor(final int code) {
        this.code = code;
    }

    /**
     * Gets the data value associated with this color
     *
     * @return An integer value of this color code
     */
    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return String.format("\u00A7%x", code);
    }

    /**
     * Gets the color represented by the specified color code
     *
     * @param code Code to check
     * @return Associative {@link Color} with the given code, or null if it doesn't exist
     */
    public static ChatColor getByCode(final int code) {
        return colors.get(code);
    }

    /**
     * Strips the given message of all color codes
     *
     * @param input String to strip of color
     * @return A copy of the input string, without any coloring
     */
    public static String stripColor(final String input) {
        if (input == null) {
            return null;
        }

        return input.replaceAll("(?i)\u00A7[0-F]", "");
    }

    /**
     * Translates alternate color codes in the given text to Minecraft color codes.
     *
     * @param altColorChar The character used to denote color codes '&'.
     * @param textToTranslate The text containing the alternate color codes.
     * @return The text with the alternate color codes replaced by Minecraft color codes.
     */
    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] charArray = textToTranslate.toCharArray();
        for (int i = 0; i < charArray.length - 1; i++) {
            if (charArray[i] == altColorChar && "0123456789AaBbCcDdEeFf".indexOf(charArray[i + 1]) > -1) {
                charArray[i] = '\u00A7';
                charArray[i + 1] = Character.toLowerCase(charArray[i + 1]);
            }
        }
        return new String(charArray);
    }

    // Tsunami start
    /**
     * Converts all color codes in the given text to their respective ANSI codes.
     *
     * @param text the text to convert
     * @return the text with all color codes converted to ANSI codes
     */
    public static String convertToAnsi(String text) {
        ANSIComponentRenderer.ToString<ChatColor> renderer = ANSIComponentRenderer.toString(AnsiStyle.instance);
        ChatColor lastColor = ChatColor.WHITE;
        renderer.pushStyle(lastColor);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\u00A7' && i < text.length() - 1) {
                ChatColor color = null;
                try {
                    color = ChatColor.getByCode(Integer.parseInt(String.valueOf(text.charAt(i + 1)), 16));
                } catch (NumberFormatException e) {
                }
                if (color != null) {
                    renderer.popStyle(lastColor);
                    lastColor = color;
                    renderer.pushStyle(lastColor);
                    i++;
                    continue;
                }
            }
            renderer.text(String.valueOf(ch));
        }
        renderer.popStyle(lastColor);
        renderer.complete();
        return renderer.asString();
    }

    private static class AnsiStyle implements StyleOps<ChatColor> {
        private static final AnsiStyle instance = new AnsiStyle();

        @Override
        public int color(ChatColor color) {
            switch (color) {
                case BLACK: return 0x000000;
                case DARK_BLUE: return 0x0000AA;
                case DARK_GREEN: return 0x00AA00;
                case DARK_AQUA: return 0x00AAAA;
                case DARK_RED: return 0xAA0000;
                case DARK_PURPLE: return 0xAA00AA;
                case GOLD: return 0xFFAA00;
                case GRAY: return 0xAAAAAA;
                case DARK_GRAY: return 0x555555;
                case BLUE: return 0x5555FF;
                case GREEN: return 0x55FF55;
                case AQUA: return 0x55FFFF;
                case RED: return 0xFF5555;
                case LIGHT_PURPLE: return 0xFF55FF;
                case YELLOW: return 0xFFFF55;
                default: return 0xFFFFFF;
            }
        }

        @Override
        public State bold(ChatColor color) {
            return State.FALSE;
        }

        @Override
        public State italics(ChatColor color) {
            return State.FALSE;
        }

        @Override
        public State underlined(ChatColor color) {
            return State.FALSE;
        }

        @Override
        public State strikethrough(ChatColor color) {
            return State.FALSE;
        }

        @Override
        public State obfuscated(ChatColor color) {
            return State.FALSE;
        }

        @Override
        public String font(ChatColor color) {
            return null;
        }

    }
    // Tsunami end

    static {
        for (ChatColor color : ChatColor.values()) {
            colors.put(color.getCode(), color);
        }
    }

}
