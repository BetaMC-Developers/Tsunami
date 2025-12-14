package org.betamc.tsunami.profile;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class GameProfile {

    private final UUID uuid;
    private final String name;
    private final boolean online;

    public GameProfile(UUID uuid, String name, boolean online) {
        Objects.requireNonNull(uuid, "uuid must not be null");
        Objects.requireNonNull(name, "name must not be null");
        this.uuid = uuid;
        this.name = name;
        this.online = online;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public boolean isOnline() {
        return this.online;
    }

    @Override
    public String toString() {
        return String.format("GameProfile{uuid=%s, name=%s, online=%b}", this.uuid, this.name, this.online);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameProfile)) return false;
        GameProfile profile = (GameProfile) o;
        return Objects.equals(this.uuid, profile.uuid)
               && Objects.equals(this.name, profile.name)
               && this.online == profile.online;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid, this.name, this.online);
    }

    public static GameProfile createOfflineProfile(String name) {
        Objects.requireNonNull(name, "name must not be null");
        UUID uuid = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
        return new GameProfile(uuid, name, false);
    }

}
