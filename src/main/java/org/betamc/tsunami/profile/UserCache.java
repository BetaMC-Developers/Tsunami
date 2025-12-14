package org.betamc.tsunami.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import org.betamc.tsunami.Tsunami;
import org.betamc.tsunami.TsunamiConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UserCache {

    private static UserCache instance;
    private static final File file = new File("usercache.json");
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
    private static final Type listType = new TypeToken<List<UserCacheEntry>>() {}.getType();
    private final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(UserCacheEntry.class, new UserCacheEntrySerializer())
            .create();

    private final ProfileFetcher profileFetcher;
    private final List<UserCacheEntry> allEntries = new ArrayList<>();
    private final Map<String, List<UserCacheEntry>> entriesByName = new HashMap<>();
    private final Map<UUID, List<UserCacheEntry>> entriesByUuid = new HashMap<>();

    public static UserCache getInstance() {
        if (instance == null) {
            instance = new UserCache();
        }
        return instance;
    }

    private UserCache() {
        this.profileFetcher = Tsunami.config().profiles().fetchMethod() == TsunamiConfig.Profiles.FetchMethod.GET
                ? new GetProfileFetcher()
                : new PostProfileFetcher();

        if (!file.exists()) {
            MinecraftServer.log.info("[Tsunami] Creating usercache.json");
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                this.gson.toJson(this.allEntries, listType, writer);
            } catch (Exception e) {
                MinecraftServer.log.severe("[Tsunami] Failed to create usercache.json: " + e);
                e.printStackTrace();
            }
        } else {
            MinecraftServer.log.info("[Tsunami] Reading usercache.json");
            try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                List<UserCacheEntry> entries = this.gson.fromJson(reader, listType);
                for (UserCacheEntry entry : entries) {
                    addProfile(entry.getProfile(), entry.getExpiresOn());
                }
            } catch (Exception e) {
                MinecraftServer.log.severe("[Tsunami] Failed to read usercache.json: " + e);
                e.printStackTrace();
            }
        }
    }

    public synchronized void save() {
        MinecraftServer.log.info("[Tsunami] Saving usercache.json");
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            this.gson.toJson(this.allEntries, listType, writer);
        } catch (Exception e) {
            MinecraftServer.log.severe("[Tsunami] Failed to save usercache.json: " + e);
            e.printStackTrace();
        }
    }

    public ProfileFetcher profileFetcher() {
        return this.profileFetcher;
    }

    public synchronized void addProfile(GameProfile profile) {
        addProfile(profile, ZonedDateTime.now().plusDays(Tsunami.config().profiles().refetchAfterDays()));
    }

    public synchronized void addProfile(GameProfile profile, ZonedDateTime expiresOn) {
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(expiresOn, "expiresOn must not be null");

        List<UserCacheEntry> entries = this.entriesByUuid.computeIfAbsent(profile.getUuid(), k -> new ArrayList<>());
        for (UserCacheEntry entry : entries) {
            if (entry.getProfile().equals(profile)) {
                entry.expiresOn = expiresOn;
                return;
            }
        }

        UserCacheEntry entry = new UserCacheEntry(profile, expiresOn);
        entries.add(entry);
        this.entriesByName.computeIfAbsent(profile.getName(), k -> new ArrayList<>()).add(entry);
        this.allEntries.add(entry);
    }

    public synchronized Optional<GameProfile> getProfile(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid must not be null");

        List<UserCacheEntry> entries = this.entriesByUuid.get(uuid);
        if (entries == null || entries.isEmpty()) return Optional.empty();
        UserCacheEntry entry = entries.get(0);
        for (UserCacheEntry e : entries) {
            if (e.getExpiresOn().isAfter(entry.getExpiresOn())) {
                entry = e;
            }
        }
        return Optional.of(entry.getProfile());
    }

    public synchronized Optional<GameProfile> getProfile(String name) {
        Objects.requireNonNull(name, "name must not be null");

        List<UserCacheEntry> entries = this.entriesByName.get(name);
        if (entries == null || entries.isEmpty()) return Optional.empty();
        UserCacheEntry entry = entries.get(0);
        for (UserCacheEntry e : entries) {
            if (e.getExpiresOn().isAfter(entry.getExpiresOn())) {
                entry = e;
            }
        }
        return Optional.of(entry.getProfile());
    }

    public synchronized Optional<GameProfile> getProfile(String name, boolean online) {
        Objects.requireNonNull(name, "name must not be null");

        List<UserCacheEntry> entries = this.entriesByName.get(name);
        if (entries == null || entries.isEmpty()) return Optional.empty();
        UserCacheEntry entry = null;
        for (UserCacheEntry e : entries) {
            if (e.getProfile().isOnline() == online
                    && (entry == null || e.getExpiresOn().isAfter(entry.getExpiresOn()))) {
                entry = e;
            }
        }
        return Optional.ofNullable(entry).map(UserCacheEntry::getProfile);
    }

    public synchronized Optional<GameProfile> getProfile(String name, boolean online, ZonedDateTime after) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(after, "after must not be null");

        List<UserCacheEntry> entries = this.entriesByName.get(name);
        if (entries == null || entries.isEmpty()) return Optional.empty();
        UserCacheEntry entry = null;
        for (UserCacheEntry e : entries) {
            if (e.getProfile().isOnline() == online
                    && e.getExpiresOn().isAfter(after)
                    && (entry == null || e.getExpiresOn().isAfter(entry.getExpiresOn()))) {
                entry = e;
            }
        }
        return Optional.ofNullable(entry).map(UserCacheEntry::getProfile);
    }

    private static class UserCacheEntry {
        private GameProfile profile;
        private ZonedDateTime expiresOn;

        UserCacheEntry(GameProfile profile, ZonedDateTime expiresOn) {
            Objects.requireNonNull(profile, "profile must not be null");
            Objects.requireNonNull(expiresOn, "expiresOn must not be null");
            this.profile = profile;
            this.expiresOn = expiresOn;
        }

        public GameProfile getProfile() {
            return profile;
        }

        public ZonedDateTime getExpiresOn() {
            return expiresOn;
        }
    }

    private static class UserCacheEntrySerializer implements JsonDeserializer<UserCacheEntry>, JsonSerializer<UserCacheEntry> {
        @Override
        public UserCacheEntry deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (!element.isJsonObject()) return null;
            JsonObject object = element.getAsJsonObject();
            try {
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                String name = object.get("name").getAsString();
                boolean online = object.get("online").getAsBoolean();
                ZonedDateTime expiresOn = ZonedDateTime.from(dtf.parse(object.get("expiresOn").getAsString()));
                return new UserCacheEntry(new GameProfile(uuid, name, online), expiresOn);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public JsonObject serialize(UserCacheEntry entry, Type type, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("uuid", entry.getProfile().getUuid().toString());
            object.addProperty("name", entry.getProfile().getName());
            object.addProperty("online", entry.getProfile().isOnline());
            object.addProperty("expiresOn", dtf.format(entry.getExpiresOn()));
            return object;
        }
    }

}
