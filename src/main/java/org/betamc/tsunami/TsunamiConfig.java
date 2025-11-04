package org.betamc.tsunami;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Paths;

@ConfigSerializable
public class TsunamiConfig {

    private static TsunamiConfig instance;

    private ChunkIo chunkIo;
    private Logging logging;
    private boolean mergeDroppedItems = false;
    private MobSpawning mobSpawning;
    private Rcon rcon;
    private Saving saving;
    private ServerListPing serverListPing;

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
            ConfigurationNode dump = CommentedConfigurationNode.root();
            mapper.save(instance, dump);
            loader.save(dump);
            return instance;
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    private TsunamiConfig() {
    }

    public ChunkIo chunkIo() {
        return chunkIo;
    }

    public Logging logging() {
        return logging;
    }

    public boolean mergeDroppedItems() {
        return mergeDroppedItems;
    }

    public MobSpawning mobSpawning() {
        return mobSpawning;
    }

    public Rcon rcon() {
        return rcon;
    }

    public Saving saving() {
        return saving;
    }

    public ServerListPing serverListPing() {
        return serverListPing;
    }

    @ConfigSerializable
    public static class ChunkIo {
        private boolean asyncLoading = false;
        private boolean asyncUnloading = false;
        private int chunkPacketCompressionLevel = 6;

        public boolean asyncLoading() {
            return asyncLoading;
        }

        public boolean asyncUnloading() {
            return asyncUnloading;
        }

        public int chunkPacketCompressionLevel() {
            return Math.min(Math.max(chunkPacketCompressionLevel, -1), 9);
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
    public static class MobSpawning {
        private MobCaps mobCaps;
        private boolean perPlayerMobCap = false;

        public MobCaps mobCaps() {
            return mobCaps;
        }

        public boolean perPlayerMobCap() {
            return perPlayerMobCap;
        }

        @ConfigSerializable
        public static class MobCaps {
            private int monsters = 70;
            private int animals = 15;
            private int waterMobs = 5;

            public int monsters() {
                return monsters;
            }

            public int animals() {
                return animals;
            }

            public int waterMobs() {
                return waterMobs;
            }
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
            return port;
        }

        public String password() {
            return password;
        }
    }

    @ConfigSerializable
    public static class Saving {
        private int worldSaveInterval = 40;
        private boolean periodicPlayerSaving = false;
        private int playerSaveInterval = 40;

        public int worldSaveInterval() {
            return worldSaveInterval;
        }

        public boolean periodicPlayerSaving() {
            return periodicPlayerSaving;
        }

        public int playerSaveInterval() {
            return playerSaveInterval;
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

}
