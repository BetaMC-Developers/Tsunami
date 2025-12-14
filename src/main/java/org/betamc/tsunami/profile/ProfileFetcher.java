package org.betamc.tsunami.profile;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ProfileFetcher {

    protected final Type profileType = new TypeToken<Optional<GameProfile>>() {}.getType();
    protected final ExecutorService executor;

    protected ProfileFetcher() {
        this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Profile fetcher").build());
    }

    public final void fetchOnlineProfile(String name, ProfileFetchCallback callback) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(callback, "callback must not be null");

        this.executor.submit(() -> fetchOnlineProfile0(name, callback));
    }

    protected abstract void fetchOnlineProfile0(String name, ProfileFetchCallback callback);

    protected final UUID parseDashlessUuid(String string) {
        return UUID.fromString(string.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

}
