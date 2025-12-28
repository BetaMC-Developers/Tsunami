package org.betamc.tsunami;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurations;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@Configuration
public class TsunamiConfig {

    private static TsunamiConfig instance;

    private Console console = new Console();
    private Logging logging = new Logging();
    private Networking networking = new Networking();
    private Profiles profiles = new Profiles();
    private Rcon rcon = new Rcon();
    private ServerListPing serverListPing = new ServerListPing();
    private Anticheat anticheat = new Anticheat();
    private World world = new World();

    public static TsunamiConfig getInstance() {
        if (instance == null) {
            instance = YamlConfigurations.update(Paths.get("tsunami.yml"), TsunamiConfig.class, builder -> builder
                    .charset(StandardCharsets.UTF_8)
                    .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
                    .inputNulls(false)
                    .outputNulls(false));
        }
        return instance;
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

    public Profiles profiles() {
        return profiles;
    }

    public Rcon rcon() {
        return rcon;
    }

    public ServerListPing serverListPing() {
        return serverListPing;
    }

    public Anticheat anticheat() {
        return anticheat;
    }

    public World world() {
        return world;
    }

    @Configuration
    public static class Console {
        @Comment({
                "The prompt which will show in console.",
                "Color codes (§[0-9a-f]) can be used here."
        })
        private String prompt = "> ";
        @Comment("If warning messages should be highlighted.")
        private boolean highlightWarnings = true;
        @Comment("If error messages should be highlighted.")
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

    @Configuration
    public static class Logging {
        @Comment("If attempts to issue unknown commands should be logged.")
        private boolean logUnknownCommands = false;

        public boolean logUnknownCommands() {
            return logUnknownCommands;
        }
    }

    @Configuration
    public static class Networking {
        @Comment({
                "The deflate compression level used to compress chunk packets.",
                "Acceptable values are [-1..9]."
        })
        private int chunkPacketCompressionLevel = 6;

        public int chunkPacketCompressionLevel() {
            return Math.min(Math.max(chunkPacketCompressionLevel, -1), 9);
        }
    }

    @Configuration
    public static class Profiles {
        @Comment({
                "The HTTP method used to fetch player profiles.",
                "Acceptable values are POST and GET."
        })
        private FetchMethod fetchMethod = FetchMethod.POST;
        @Comment("The URL which should be used for POST requests.")
        private String postUrl = "https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname";
        @Comment("The URL which should be used for GET requests. {username} will be replaced by the actual name.")
        private String getUrl = "https://api.minecraftservices.com/minecraft/profile/lookup/name/{username}";
        @Comment("If the name of a player with an online profile is required to exactly match the name returned by the API.")
        private boolean verifyUsernameCasing = false;
        @Comment({
                "Specifies in which cases offline profiles should be created for players.",
                "Acceptable values are NEVER, WHEN_CRACKED and ALWAYS."
        })
        private CreateOfflineProfiles createOfflineProfiles = CreateOfflineProfiles.NEVER;
        @Comment("After how many days online profiles should be refetched.")
        private int refetchAfterDays = 30;

        public FetchMethod fetchMethod() {
            return fetchMethod;
        }

        public String postUrl() {
            return postUrl;
        }

        public String getUrl() {
            return getUrl;
        }

        public boolean verifyUsernameCasing() {
            return verifyUsernameCasing;
        }

        public CreateOfflineProfiles createOfflineProfiles() {
            return createOfflineProfiles;
        }

        public int refetchAfterDays() {
            return Math.max(refetchAfterDays, 0);
        }

        public enum FetchMethod {
            POST,
            GET
        }

        public enum CreateOfflineProfiles {
            NEVER,
            WHEN_CRACKED,
            ALWAYS
        }
    }

    @Configuration
    public static class Rcon {
        @Comment({
                "If the remote console protocol should be enabled.",
                "Please note that RCON is not encrypted and should not be used in a production environment."
        })
        private boolean enabled = false;
        @Comment("The port used to listen for RCON connections.")
        private int port = 25575;
        @Comment("The password required to establish an RCON connection.")
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

    @Configuration
    public static class ServerListPing {
        @Comment("If the 1.7+ query protocol should be enabled.")
        private boolean enabled = false;
        @Comment({
                "The MOTD which should be included in the query response.",
                "Color codes (§[0-9a-f]) can be used here."
        })
        private String motd = "A Minecraft Server";
        @Comment("If the names of connected players should be included in the query response.")
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

    @Configuration
    public static class Anticheat {
        private FlagQuickMovement flagQuickMovement = new FlagQuickMovement();
        private FlagWrongMovement flagWrongMovement = new FlagWrongMovement();
        private FlagFlight flagFlight = new FlagFlight();

        public FlagQuickMovement flagQuickMovement() {
            return flagQuickMovement;
        }

        public FlagWrongMovement flagWrongMovement() {
            return flagWrongMovement;
        }

        public FlagFlight flagFlight() {
            return flagFlight;
        }

        @Configuration
        public static class FlagQuickMovement {
            @Comment({
                    "If too quick movement should be flagged.",
                    "Players with the permission 'tsunami.anticheat.quick-movement.bypass' are exempt from being flagged."
            })
            private boolean enabled = true;
            @Comment("The distance threshold for quick movement to be flagged.")
            private double threshold = 100.0;
            @Comment({
                    "If flagged players should be teleported back to their previous location.",
                    "If this is disabled, players will be kicked instead."
            })
            private boolean teleportBack = false;

            public boolean enabled() {
                return enabled;
            }

            public double threshold() {
                return threshold;
            }

            public boolean teleportBack() {
                return teleportBack;
            }
        }

        @Configuration
        public static class FlagWrongMovement {
            @Comment({
                    "If wrong movement should be flagged.",
                    "Players with the permission 'tsunami.anticheat.wrong-movement.bypass' are exempt from being flagged."
            })
            private boolean enabled = true;
            @Comment("The distance threshold for wrong movement to be flagged.")
            private double threshold = 0.0625;
            @Comment("If flagged players should be teleported back to their previous location.")
            private boolean teleportBack = true;

            public boolean enabled() {
                return enabled;
            }

            public double threshold() {
                return threshold;
            }

            public boolean teleportBack() {
                return teleportBack;
            }
        }

        @Configuration
        public static class FlagFlight {
            @Comment({
                    "After how many ticks players should be kicked for flying.",
                    "Players with the permission 'tsunami.anticheat.flight.bypass' are exempt from being flagged."
            })
            private int kickAfter = 80;

            public int kickAfter() {
                return kickAfter;
            }
        }
    }

    @Configuration
    public static class World {
        private AsyncChunkLoading asyncChunkLoading = new AsyncChunkLoading();
        @Comment("The interval in ticks in which world data should be auto-saved.")
        private int autoSaveInterval = 6000;
        @Comment("The maximum amount of chunks to auto-save per tick.")
        private int maxAutoSaveChunksPerTick = 24;
        private MobCaps mobCaps = new MobCaps();
        @Comment("If mob caps should be enforced on a per-player basis instead of globally.")
        private boolean perPlayerMobSpawning = false;
        @Comment("If dropped items should merge if they are of the same type.")
        private boolean mergeDroppedItems = false;

        public AsyncChunkLoading asyncChunkLoading() {
            return asyncChunkLoading;
        }

        public int autoSaveInterval() {
            return Math.max(autoSaveInterval, 1);
        }

        public int maxAutoSaveChunksPerTick() {
            return Math.max(maxAutoSaveChunksPerTick, 1);
        }

        public MobCaps mobCaps() {
            return mobCaps;
        }

        public boolean perPlayerMobSpawning() {
            return perPlayerMobSpawning;
        }

        public boolean mergeDroppedItems() {
            return mergeDroppedItems;
        }

        @Configuration
        public static class AsyncChunkLoading {
            @Comment("If chunks should be loaded from disk asynchronously.")
            private boolean enabled = false;
            @Comment("The amount of threads to use for loading chunks.")
            private int threads = 3;

            public boolean enabled() {
                return enabled;
            }

            public int threads() {
                return Math.max(threads, 1);
            }
        }

        @Configuration
        public static class MobCaps {
            @Comment("The mob cap for hostile mobs.")
            private int monsters = 70;
            @Comment("The mob cap for passive mobs.")
            private int animals = 15;
            @Comment("The mob cap for water mobs (squids).")
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
