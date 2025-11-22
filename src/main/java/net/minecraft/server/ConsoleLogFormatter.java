package net.minecraft.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

final class ConsoleLogFormatter extends Formatter {

    private static final SimpleDateFormat a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Tsunami - static final
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\e\\[[\\d;]*[^\\d;]"); // Tsunami

    ConsoleLogFormatter() {}

    public String format(LogRecord logrecord) {
        StringBuilder stringbuilder = new StringBuilder();

        stringbuilder.append(a.format(Long.valueOf(logrecord.getMillis())));
        Level level = logrecord.getLevel();

        if (level == Level.FINEST) {
            stringbuilder.append(" [FINEST] ");
        } else if (level == Level.FINER) {
            stringbuilder.append(" [FINER] ");
        } else if (level == Level.FINE) {
            stringbuilder.append(" [FINE] ");
        } else if (level == Level.INFO) {
            stringbuilder.append(" [INFO] ");
        } else if (level == Level.WARNING) {
            stringbuilder.append(" [WARNING] ");
        } else if (level == Level.SEVERE) {
            stringbuilder.append(" [SEVERE] ");
        } else if (level == Level.SEVERE) {
            stringbuilder.append(" [" + level.getLocalizedName() + "] ");
        }

        // Tsunami start - get rid of ansi codes
        String message = ANSI_PATTERN.matcher(logrecord.getMessage()).replaceAll("");
        stringbuilder.append(message);
        // Tsunami end
        stringbuilder.append('\n');
        Throwable throwable = logrecord.getThrown();

        if (throwable != null) {
            StringWriter stringwriter = new StringWriter();

            throwable.printStackTrace(new PrintWriter(stringwriter));
            stringbuilder.append(stringwriter.toString());
        }

        return stringbuilder.toString();
    }
}
