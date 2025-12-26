package com.legacyminecraft.poseidon.utility;

import com.legacyminecraft.poseidon.PoseidonConfig;
import org.bukkit.craftbukkit.CraftServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class PoseidonVersionChecker {

    // Tsunami - change urls to Tsunami repository
    private static final String GITHUB_API_URL = "https://api.github.com/repos/BetaMC-Developers/Tsunami/releases/latest";
    private static final String releaseUrl = "https://github.com/BetaMC-Developers/Tsunami/releases/tag/";
    private final String currentVersion;
    private volatile String latestVersion;
    private CraftServer server;

    public PoseidonVersionChecker(CraftServer server, String currentVersion) {
        this.currentVersion = currentVersion;
        this.latestVersion = currentVersion; // Assume the latest version is the current version until checked
        this.server = server;
    }

    /**
     * Checks if a new version is available.
     *
     * @return true if a newer version is available, false otherwise.
     */
    public synchronized boolean isUpdateAvailable() {
        return latestVersion != null && !currentVersion.equalsIgnoreCase(latestVersion);
    }

    /**
     * Fetches the latest release version from GitHub API.
     *
     * @return the latest version as a String or null if fetching fails.
     */
    public void fetchLatestVersion() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(GITHUB_API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                server.getLogger().log(Level.WARNING, "[Tsunami] Failed to check GitHub for latest version. HTTP Response Code: " + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response.toString());

            this.latestVersion = (String) json.get("tag_name");

            if (isUpdateAvailable()) {
                server.getLogger().log(Level.INFO, "[Tsunami] A new version is available: " + latestVersion);
                server.getLogger().log(Level.INFO, "[Tsunami] You are currently running version: " + currentVersion);
                server.getLogger().log(Level.INFO, "[Tsunami] Download the latest version here: " + releaseUrl + latestVersion); // Tsunami - print direct link
            } else {
                if (PoseidonConfig.getInstance().getConfigBoolean("settings.update-checker.notify-if-up-to-date.enabled"))
                    server.getLogger().log(Level.INFO, "[Tsunami] You are running the latest version (" + currentVersion + ") of Tsunami.");
            }
        } catch (Exception e) {
            server.getLogger().log(Level.WARNING, "[Tsunami] Failed to check GitHub for latest version.", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
