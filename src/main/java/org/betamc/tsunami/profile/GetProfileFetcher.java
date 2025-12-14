package org.betamc.tsunami.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.betamc.tsunami.Tsunami;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

public class GetProfileFetcher extends ProfileFetcher {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(this.profileType, new GetResponseDeserializer())
            .create();

    @Override
    protected void fetchOnlineProfile0(String name, ProfileFetchCallback callback) {
        Optional<GameProfile> profile;

        try {
            URL url = new URL(Tsunami.config().profiles().getUrl().replace("{username}", name));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                profile = this.gson.fromJson(reader, this.profileType);
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
    }

    private class GetResponseDeserializer implements JsonDeserializer<Optional<GameProfile>> {
        @Override
        public Optional<GameProfile> deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (!element.isJsonObject()) return null;
            JsonObject object = element.getAsJsonObject();

            try {
                UUID uuid = parseDashlessUuid(object.get("id").getAsString());
                String name = object.get("name").getAsString();
                return Optional.of(new GameProfile(uuid, name, true));
            } catch (Exception e) {
                return null;
            }
        }
    }

}
