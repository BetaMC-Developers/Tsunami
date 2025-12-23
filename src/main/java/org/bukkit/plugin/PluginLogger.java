package org.bukkit.plugin;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The PluginLogger class is a modified {@link Logger} that prepends all
 * logging calls with the name of the plugin doing the logging. The API for
 * PluginLogger is exactly the same as {@link Logger}.
 *
 * @see Logger
 */
public class PluginLogger extends Logger {

    private final String logPrefix;

    /**
     * Creates a new PluginLogger that extracts the name from a plugin.
     *
     * @param plugin A reference to the plugin
     */
    public PluginLogger(Plugin plugin) {
        super(plugin.getClass().getCanonicalName(), null);
        String prefix = plugin.getDescription().getPrefix();
        this.logPrefix = "[" + (prefix != null ? prefix : plugin.getDescription().getName()) + "] ";
        setParent(plugin.getServer().getLogger());
        setLevel(Level.ALL);
    }

    @Override
    public void log(LogRecord logRecord) {
        logRecord.setMessage(logPrefix + logRecord.getMessage());
        super.log(logRecord);
    }

}
