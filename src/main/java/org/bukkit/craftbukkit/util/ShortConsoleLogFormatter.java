package org.bukkit.craftbukkit.util;

import joptsimple.OptionException;
import joptsimple.OptionSet;
import net.minecraft.server.MinecraftServer;
import org.betamc.tsunami.Tsunami;
import org.bukkit.ChatColor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ShortConsoleLogFormatter extends Formatter {
    private final SimpleDateFormat date;

    public ShortConsoleLogFormatter(MinecraftServer server) {
        OptionSet options = server.options;
        SimpleDateFormat date = null;

        if (options.has("date-format")) {
            try {
                Object object = options.valueOf("date-format");

                if ((object != null) && (object instanceof SimpleDateFormat)) {
                    date = (SimpleDateFormat) object;
                }
            } catch (OptionException ex) {
                System.err.println("Given date format is not valid. Falling back to default.");
            }
        } else if (options.has("nojline")) {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        if (date == null) {
            date = new SimpleDateFormat("HH:mm:ss");
        }

        this.date = date;
    }

    @Override
    public String format(LogRecord record) {
        String formattedMessage = super.formatMessage(record); // Tsunami
        StringBuilder builder = new StringBuilder();
        Throwable ex = record.getThrown();

        builder.append(date.format(record.getMillis()));
        builder.append(" [");
        builder.append(record.getLevel().getLocalizedName().toUpperCase());
        builder.append("] ");
        builder.append(formattedMessage);
        // Tsunami - removed builder.append('\n');

        if (ex != null) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            builder.append(writer);
        }

        // Tsunami start
        if (record.getLevel().equals(Level.WARNING) && Tsunami.config().console().highlightWarnings()) {
            return ChatColor.convertToAnsi(ChatColor.YELLOW + builder.toString());
        } else if (record.getLevel().equals(Level.SEVERE) && Tsunami.config().console().highlightErrors()) {
            return ChatColor.convertToAnsi(ChatColor.RED + builder.toString());
        }
        // Tsunami end
        return builder.toString();
    }

}
