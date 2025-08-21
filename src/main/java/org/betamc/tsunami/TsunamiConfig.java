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
        writeDefault("merge-dropped-items", false);
        writeDefault("mob-spawning.mob-caps.monsters", 70);
        writeDefault("mob-spawning.mob-caps.animals", 15);
        writeDefault("mob-spawning.mob-caps.water-mobs", 5);
        writeDefault("mob-spawning.per-player-mob-cap", false);
    }

    private void writeDefault(String key, Object defaultValue) {
        if (getProperty(key) == null) {
            setProperty(key, defaultValue);
        }
    }

}
