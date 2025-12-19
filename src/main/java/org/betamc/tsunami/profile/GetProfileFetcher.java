package org.betamc.tsunami.profile;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.betamc.tsunami.Tsunami;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetProfileFetcher implements ProfileFetcher {

    private static final Type responseType = new TypeToken<GameProfile>() {}.getType();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("GET Profile Fetcher").build());
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(responseType, new GetResponseDeserializer())
            .create();

    @Override
    public void fetchOnlineProfile(String name, ProfileFetchCallback callback) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(callback, "callback must not be null");

        this.executor.submit(() -> {
            Optional<GameProfile> profile;
            try {
                URL url = new URL(Tsunami.config().profiles().getUrl().replace("{username}", name));
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    profile = Optional.of(this.gson.fromJson(reader, responseType));
                }
            } catch (Exception e) {
                if (e instanceof FileNotFoundException) {
                    profile = Optional.empty();
                } else {
                    callback.onFailure(e);
                    return;
                }
            }

            callback.onSuccess(profile);
        });
    }

    private static class GetResponseDeserializer implements JsonDeserializer<GameProfile> {
        @Override
        public GameProfile deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            try {
                JsonObject object = element.getAsJsonObject();
                UUID uuid = ProfileFetcher.parseDashlessUuid(object.get("id").getAsString());
                String name = object.get("name").getAsString();
                return new GameProfile(uuid, name, true);
            } catch (Exception e) {
                throw new JsonParseException("Failed to parse JSON element into GameProfile response", e);
            }
        }
    }

}
