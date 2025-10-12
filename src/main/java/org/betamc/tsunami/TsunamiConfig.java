package org.betamc.tsunami;

import org.bukkit.util.config.Configuration;

import java.io.File;

public class TsunamiConfig extends Configuration {

    private static TsunamiConfig instance;

    public static TsunamiConfig getInstance() {
        if (instance == null) {
            instance = new TsunamiConfig();
        }
        return instance;
    }

    private TsunamiConfig() {
        super(new File("tsunami.yml"));
        init();
    }

    private void init() {
        load();
        writeDefaults();
        save();
    }

    private void writeDefaults() {
        writeDefault("chunk-io.async-loading", false);
        writeDefault("chunk-io.async-unloading", false);
        writeDefault("chunk-io.chunk-packet-compression-level", 6);
        writeDefault("logging.log-unknown-commands", false);
        writeDefault("merge-dropped-items", false);
        writeDefault("mob-spawning.mob-caps.monsters", 70);
        writeDefault("mob-spawning.mob-caps.animals", 15);
        writeDefault("mob-spawning.mob-caps.water-mobs", 5);
        writeDefault("mob-spawning.per-player-mob-cap", false);
        writeDefault("rcon.enabled", false);
        writeDefault("rcon.port", 25575);
        writeDefault("rcon.password", "");
        writeDefault("saving.world-save-interval", 40);
        writeDefault("saving.periodic-player-saving", false);
        writeDefault("saving.player-save-interval", 40);
        writeDefault("server-list-ping.enabled", false);
        writeDefault("server-list-ping.motd", "A Minecraft Server");
        writeDefault("server-list-ping.show-player-names", true);
    }

    private void writeDefault(String key, Object defaultValue) {
        if (getProperty(key) == null) {
            setProperty(key, defaultValue);
        }
    }

}
