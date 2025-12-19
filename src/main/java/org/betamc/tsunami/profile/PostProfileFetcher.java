package org.betamc.tsunami.profile;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.betamc.tsunami.Tsunami;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostProfileFetcher implements ProfileFetcher {

    private static final Type responseType = new TypeToken<Optional<GameProfile>>() {}.getType();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("POST Profile Fetcher").build());
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(responseType, new PostResponseDeserializer())
            .create();

    @Override
    public void fetchOnlineProfile(String name, ProfileFetchCallback callback) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(callback, "callback must not be null");

        this.executor.submit(() -> {
            Optional<GameProfile> profile;
            try {
                URL url = new URL(Tsunami.config().profiles().postUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
                    JsonArray array = new JsonArray();
                    array.add(name);
                    this.gson.toJson(array, writer);
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    profile = this.gson.fromJson(reader, responseType);
                }
            } catch (Exception e) {
                callback.onFailure(e);
                return;
            }

            callback.onSuccess(profile);
        });
    }

    private static class PostResponseDeserializer implements JsonDeserializer<Optional<GameProfile>> {
        @Override
        public Optional<GameProfile> deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            try {
                JsonArray array = element.getAsJsonArray();
                if (array.isEmpty()) return Optional.empty();
                JsonObject object = array.get(0).getAsJsonObject();
                UUID uuid = ProfileFetcher.parseDashlessUuid(object.get("id").getAsString());
                String name = object.get("name").getAsString();
                return Optional.of(new GameProfile(uuid, name, true));
            } catch (Exception e) {
                throw new JsonParseException("Failed to parse JSON element into GameProfile response", e);
            }
        }
    }

}
