package com.projectposeidon.johnymuffin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import org.betamc.tsunami.Tsunami;
import org.betamc.tsunami.profile.GameProfile;

import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UUIDManager {
    private static UUIDManager singleton;
    //private JSONArray UUIDJsonArray; // Tsunami

    private UUIDManager() {
        // Tsunami start - uuidcache.json -> usercache.json migration
        File uuidcacheJson = new File("uuidcache.json");
        File usercacheJson = new File("usercache.json");
        if (!uuidcacheJson.exists() || usercacheJson.exists()) return;

        MinecraftServer.log.info("");
        MinecraftServer.log.info("********************************************************************************");
        MinecraftServer.log.info("Migrating uuidcache.json to usercache.json to replicate modern server software more accurately.");
        MinecraftServer.log.info("Note that many configurations related to UUIDs in the Poseidon config are no longer effective.");
        MinecraftServer.log.info("Please make sure to set these configurations accordingly in the 'profiles' section of the Tsunami config.");
        MinecraftServer.log.info("********************************************************************************");
        MinecraftServer.log.info("");

        try {
            List<UuidCacheEntry> entries;
            try (BufferedReader reader = Files.newBufferedReader(uuidcacheJson.toPath(), StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<List<UuidCacheEntry>>() {}.getType();
                Gson gson = new GsonBuilder()
                        .registerTypeHierarchyAdapter(UuidCacheEntry.class, new UuidCacheEntryDeserializer())
                        .create();
                entries = gson.fromJson(reader, listType);
            }

            Tsunami.userCache();
            for (UuidCacheEntry entry : entries) {
                GameProfile profile = new GameProfile(entry.uuid, entry.name, entry.onlineUUID);
                ZonedDateTime expiresOn = ZonedDateTime.ofInstant(Instant.ofEpochSecond(entry.expiresOn), ZoneId.systemDefault());
                Tsunami.userCache().addProfile(profile, expiresOn);
            }
            Tsunami.userCache().save();

            MinecraftServer.log.info("[Tsunami] Successfully migrated uuidcache.json to usercache.json");
        } catch (Throwable e) {
            MinecraftServer.log.severe("[Tsunami] Failed to migrate uuidcache.json to usercache.json");
            e.printStackTrace();
        }
        // Tsunami end
    }

    public UUID getUUIDGraceful(String username) {
        // Tsunami start
        return Tsunami.userCache().getProfile(username, true)
                .orElse(GameProfile.createOfflineProfile(username))
                .getUuid();
        // Tsunami end
    }

    public static UUID generateOfflineUUID(String username) {
        return GameProfile.createOfflineProfile(username).getUuid(); // Tsunami
    }

    public void saveJsonArray() {
        // Tsunami - no-op
    }

    public void receivedUUID(String username, UUID uuid, Long expiry, boolean online) {
        // Tsunami start
        ZonedDateTime expiresOn = ZonedDateTime.ofInstant(Instant.ofEpochSecond(expiry), ZoneId.systemDefault());
        GameProfile profile = new GameProfile(uuid, username, online);
        Tsunami.userCache().addProfile(profile, expiresOn);
        // Tsunami end
    }

    // Tsunami - removed addUser()

    public UUID getUUIDFromUsername(String username) {
        return Tsunami.userCache().getProfile(username).map(GameProfile::getUuid).orElse(null); // Tsunami
    }

    public UUID getUUIDFromUsername(String username, boolean online) {
        return Tsunami.userCache().getProfile(username, online).map(GameProfile::getUuid).orElse(null); // Tsunami
    }

    public UUID getUUIDFromUsername(String username, boolean online, Long afterUnix) {
        // Tsunami start
        ZonedDateTime after = ZonedDateTime.ofInstant(Instant.ofEpochSecond(afterUnix), ZoneId.systemDefault());
        return Tsunami.userCache().getProfile(username, online, after).map(GameProfile::getUuid).orElse(null);
        // Tsunami end
    }

    public String getUsernameFromUUID(UUID uuid) {
        return Tsunami.userCache().getProfile(uuid).map(GameProfile::getName).orElse(null); // Tsunami
    }

    // Tsunami - removed removeInstancesOfUsername() and removeInstancesOfUUID()

    public static UUIDManager getInstance() {
        if (UUIDManager.singleton == null) {
            UUIDManager.singleton = new UUIDManager();
        }
        return UUIDManager.singleton;
    }

    // Tsunami start
    private static class UuidCacheEntry {
        private UUID uuid;
        private String name;
        private boolean onlineUUID;
        private long expiresOn;

        public UuidCacheEntry(UUID uuid, String name, boolean onlineUUID, long expiresOn) {
            Objects.requireNonNull(uuid, "uuid must not be null");
            Objects.requireNonNull(name, "name must not be null");
            this.uuid = uuid;
            this.name = name;
            this.onlineUUID = onlineUUID;
            this.expiresOn = expiresOn;
        }
    }

    private static class UuidCacheEntryDeserializer implements JsonDeserializer<UuidCacheEntry> {
        @Override
        public UuidCacheEntry deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            try {
                JsonObject object = element.getAsJsonObject();
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                String name = object.get("name").getAsString();
                boolean onlineUUID = object.get("onlineUUID").getAsBoolean();
                long expiresOn = object.get("expiresOn").getAsLong();
                return new UuidCacheEntry(uuid, name, onlineUUID, expiresOn);
            } catch (Exception e) {
                throw new JsonParseException("Failed to parse JSON element into UUID cache entry", e);
            }
        }
    }
    // Tsunami end

}
