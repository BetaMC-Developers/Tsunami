package org.betamc.tsunami;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Paths;

@ConfigSerializable
public class TsunamiConfig {

    private static final String HEADER =
            "Tsunami configuration file\n\n" +
            "Notice:\n" +
            "- You must use spaces for indentation, NOT tabs.\n" +
            "- Durations are measured in game ticks.";

    private static TsunamiConfig instance;

    private Console console;
    private Logging logging;
    private Networking networking;
    private Rcon rcon;
    private ServerListPing serverListPing;
    private World world;

    public static TsunamiConfig getInstance() {
        if (instance != null) return instance;

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(Paths.get("tsunami.yml"))
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK)
                .build();

        try {
            ConfigurationNode node = loader.load();
            ObjectMapper<TsunamiConfig> mapper = ObjectMapper.factory().get(TsunamiConfig.class);
            instance = mapper.load(node);
            ConfigurationNode dump = CommentedConfigurationNode.root(ConfigurationOptions.defaults().header(HEADER));
            mapper.save(instance, dump);
            loader.save(dump);
            return instance;
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    private TsunamiConfig() {
    }

    public Console console() {
        return console;
    }

    public Logging logging() {
        return logging;
    }

    public Networking networking() {
        return networking;
    }

    public Rcon rcon() {
        return rcon;
    }

    public ServerListPing serverListPing() {
        return serverListPing;
    }

    public World world() {
        return world;
    }

    @ConfigSerializable
    public static class Console {
        private String prompt = "> ";
        private boolean highlightWarnings = true;
        private boolean highlightErrors = true;

        public String prompt() {
            return prompt;
        }

        public boolean highlightWarnings() {
            return highlightWarnings;
        }

        public boolean highlightErrors() {
            return highlightErrors;
        }
    }

    @ConfigSerializable
    public static class Logging {
        private boolean logUnknownCommands = false;

        public boolean logUnknownCommands() {
            return logUnknownCommands;
        }
    }

    @ConfigSerializable
    public static class Networking {
        private int chunkPacketCompressionLevel = 6;

        public int chunkPacketCompressionLevel() {
            return Math.min(Math.max(chunkPacketCompressionLevel, -1), 9);
        }
    }

    @ConfigSerializable
    public static class Rcon {
        private boolean enabled = false;
        private int port = 25575;
        private String password = "";

        public boolean enabled() {
            return enabled;
        }

        public int port() {
            return Math.max(port, 0);
        }

        public String password() {
            return password;
        }
    }

    @ConfigSerializable
    public static class ServerListPing {
        private boolean enabled = false;
        private String motd = "A Minecraft Server";
        private boolean showPlayerNames = true;

        public boolean enabled() {
            return enabled;
        }

        public String motd() {
            return motd;
        }

        public boolean showPlayerNames() {
            return showPlayerNames;
        }
    }

    @ConfigSerializable
    public static class World {
        private AsyncChunkLoading asyncChunkLoading;
        private int autoSaveInterval = 40;
        private AutoPlayerSaving autoPlayerSaving;
        private MobCaps mobCaps;
        private boolean perPlayerMobCap = false;
        private boolean mergeDroppedItems = false;

        public AsyncChunkLoading asyncChunkLoading() {
            return asyncChunkLoading;
        }

        public int autoSaveInterval() {
            return Math.max(autoSaveInterval, 1);
        }

        public AutoPlayerSaving autoPlayerSaving() {
            return autoPlayerSaving;
        }

        public MobCaps mobCaps() {
            return mobCaps;
        }

        public boolean perPlayerMobCap() {
            return perPlayerMobCap;
        }

        public boolean mergeDroppedItems() {
            return mergeDroppedItems;
        }

        @ConfigSerializable
        public static class AsyncChunkLoading {
            private boolean enabled = false;
            private int threads = 3;

            public boolean enabled() {
                return enabled;
            }

            public int threads() {
                return Math.max(threads, 1);
            }
        }

        @ConfigSerializable
        public static class AutoPlayerSaving {
            private boolean enabled = false;
            private int interval = 40;

            public boolean enabled() {
                return enabled;
            }

            public int interval() {
                return Math.max(interval, 1);
            }
        }

        @ConfigSerializable
        public static class MobCaps {
            private int monsters = 70;
            private int animals = 15;
            private int waterMobs = 5;

            public int monsters() {
                return Math.max(monsters, 0);
            }

            public int animals() {
                return Math.max(animals, 0);
            }

            public int waterMobs() {
                return Math.max(waterMobs, 0);
            }
        }
    }

}
