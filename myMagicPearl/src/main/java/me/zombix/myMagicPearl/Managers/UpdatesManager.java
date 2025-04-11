package me.zombix.myMagicPearl.Managers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.bukkit.Bukkit.getLogger;

public class UpdatesManager {
    private final JavaPlugin plugin;
    private final String pluginName;
    private final String currentVersion;
    private final String repoOwner;
    private final String repoName;
    private final List<String> configFiles = Arrays.asList("config.yml", "messages.yml");

    public UpdatesManager(String pluginName, String currentVersion, String repoOwner, String repoName, JavaPlugin plugin) {
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
                InputStreamReader reader = new InputStreamReader(inputStream);

                JsonObject jsonObject = parseJson(reader);
                if (jsonObject != null) {
                    String latestVersion = jsonObject.get("tag_name").getAsString();
                    return !latestVersion.equals(currentVersion);
                }
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
                InputStreamReader reader = new InputStreamReader(inputStream);

                JsonObject jsonObject = parseJson(reader);
                if (jsonObject != null) {
                    return jsonObject.get("tag_name").getAsString().replace("v", "");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JsonObject parseJson(InputStreamReader reader) {
        try {
            JsonElement jsonElement;
            try {
                jsonElement = JsonParser.parseReader(reader);
            } catch (NoSuchMethodError e) {
                jsonElement = new JsonParser().parse(reader);
            }
            return jsonElement.getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updatePlugin(CommandSender sender) {
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

                File newJarFile = new File("plugins/" + pluginName + "-" + latestVersion.replace("v", "") + ".jar");
                for (String configFileName : configFiles) {
                    File newConfigFile = extractConfigFromJar(newJarFile, configFileName);
                    if (newConfigFile != null) {
                        File oldConfigFile = new File(plugin.getDataFolder(), configFileName);
                        integrateConfigs(newConfigFile, oldConfigFile);
                        newConfigFile.delete();
                    }
                }

                loadPlugin(latestVersion);
                if (sender != null & sender instanceof Player) {
                    sender.sendMessage(ChatColor.GREEN + "Plugin was successfully updated!");
                }
                getLogger().info("[myIncognito] Plugin was successfully updated!");
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
                unregisterAllListeners(targetPlugin);
                unregisterAllCommands(targetPlugin);
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

    private void loadPlugin(String latestVersion) {
        try {
            PluginManager pluginManager = Bukkit.getPluginManager();
            File newFile = new File("plugins/" + pluginName + "-" + latestVersion.replace("v", "") + ".jar");

            if (newFile.exists()) {
                Plugin newPlugin = pluginManager.loadPlugin(newFile);
                pluginManager.enablePlugin(newPlugin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File extractConfigFromJar(File jarFile, String configFileName) {
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry configEntry = jar.getJarEntry(configFileName);

            if (configEntry != null) {
                InputStream inputStream = jar.getInputStream(configEntry);
                File newConfigFile = new File("plugins/" + jarFile.getName() + "_" + configFileName);

                try (FileOutputStream fileOutputStream = new FileOutputStream(newConfigFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                }

                return newConfigFile;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void integrateConfigs(File newConfigFile, File oldConfigFile) {
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile);
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(newConfigFile);

        for (String key : newConfig.getKeys(true)) {
            if (oldConfig.contains(key)) {
                newConfig.set(key, oldConfig.get(key));
            }
        }

        try {
            newConfig.save(oldConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unregisterAllListeners(Plugin plugin) {
        HandlerList.unregisterAll(plugin);
    }

    public void unregisterAllCommands(Plugin plugin) {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getServer());

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            knownCommands.entrySet().removeIf(entry -> {
                Command command = entry.getValue();

                if (command instanceof PluginCommand) {
                    PluginCommand pluginCommand = (PluginCommand) command;
                    return pluginCommand.getPlugin().equals(plugin);
                }

                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
