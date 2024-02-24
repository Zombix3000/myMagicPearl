package me.zombix.mymagicpearl.Config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Updates {

    private final JavaPlugin plugin;
    private final String pluginName;
    private final String currentVersion;
    private final String repoOwner;
    private final String repoName;

    public Updates(String pluginName, String currentVersion, String repoOwner, String repoName, JavaPlugin plugin) {
        this.pluginName = pluginName;
        this.currentVersion = currentVersion;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.plugin = plugin;
    }

    public boolean checkForUpdates() {
        try {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", repoOwner, repoName);
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
                String latestVersion = jsonObject.get("tag_name").getAsString();

                return !latestVersion.equals(currentVersion);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getLatestVersion() {
        try {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", repoOwner, repoName);
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
                return jsonObject.get("tag_name").getAsString().replace("v", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePlugin() {
        String latestVersion = getLatestVersion();
        if (latestVersion != null && !latestVersion.equals(currentVersion)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                File oldFile = new File("plugins/" + pluginName + "-" + currentVersion.replace("v", "") + ".jar");
                if (oldFile.exists()) {
                    if (unloadPlugin()) {
                        oldFile.delete();
                    }
                }

                downloadJarFile(latestVersion);

                loadPlugin(latestVersion);
            }, 20L);
        }
    }

    public void downloadJarFile(String latestVersion) {
        try {
            String downloadUrl = String.format("https://github.com/%s/%s/releases/latest/download/%s.jar", repoOwner, repoName, pluginName + "-" + latestVersion.replace("v", ""));
            URL url = new URL(downloadUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream fileOutputStream = new FileOutputStream("plugins/" + pluginName + "-" + latestVersion.replace("v", "") + ".jar")) {

                byte[] dataBuffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean unloadPlugin() {
        try {
            PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin targetPlugin = pluginManager.getPlugin(pluginName);

            if (targetPlugin != null) {
                pluginManager.disablePlugin(targetPlugin);
                Map<String, Plugin> knownPlugins = new HashMap<>();
                for (Plugin plugin : pluginManager.getPlugins()) {
                    knownPlugins.put(plugin.getName(), plugin);
                }
                knownPlugins.remove(pluginName);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadPlugin(String latestVersion) {
        try {
            PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin targetPlugin = pluginManager.getPlugin(pluginName);

            if (targetPlugin != null) {
                File newFile = new File("plugins/" + pluginName + "-" + latestVersion.replace("v", "") + ".jar");
                try {
                    Plugin newPlugin = pluginManager.loadPlugin(newFile);
                    pluginManager.enablePlugin(newPlugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
