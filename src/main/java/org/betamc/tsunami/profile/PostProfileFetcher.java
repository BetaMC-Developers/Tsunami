package org.betamc.tsunami.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.betamc.tsunami.Tsunami;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

public class PostProfileFetcher extends ProfileFetcher {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(this.profileType, new PostResponseDeserializer())
            .create();

    @Override
    protected void fetchOnlineProfile0(String name, ProfileFetchCallback callback) {
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
                profile = this.gson.fromJson(reader, this.profileType);
            }
        } catch (Exception e) {
            callback.onFailure(e);
            return;
        }

        callback.onSuccess(profile);
    }

    private class PostResponseDeserializer implements JsonDeserializer<Optional<GameProfile>> {
        @Override
        public Optional<GameProfile> deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (!element.isJsonArray()) return null;
            JsonArray array = element.getAsJsonArray();
            if (array.isEmpty()) return Optional.empty();

            try {
                JsonObject object = array.get(0).getAsJsonObject();
                UUID uuid = parseDashlessUuid(object.get("id").getAsString());
                String name = object.get("name").getAsString();
                return Optional.of(new GameProfile(uuid, name, true));
            } catch (Exception e) {
                return null;
            }
        }
    }

}
