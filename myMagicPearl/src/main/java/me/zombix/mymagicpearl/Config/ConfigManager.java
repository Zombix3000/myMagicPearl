package me.zombix.mymagicpearl.Config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration permissionsConfig;
    private final File configFile;
    private final File messagesFile;
    private final File permissionsFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.permissionsFile = new File(plugin.getDataFolder(), "permissions.yml");
    }

    public void setupConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        if (!permissionsFile.exists()) {
            plugin.saveResource("permissions.yml", false);
        }

        mainConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        messagesConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        permissionsConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "permissions.yml"));
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    public FileConfiguration getPermissionsConfig() {
        return permissionsConfig;
    }

    public void saveMainConfig() {
        try {
            mainConfig.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save config.yml!");
        }
    }

    public void savePermissionsConfig() {
        try {
            permissionsConfig.save(permissionsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save permissions.yml!");
        }
    }

    public int getCooldownForPermission(Player player) {
        List<Integer> cooldown = new ArrayList<>();

        cooldown.add(mainConfig.getInt("pearl.default-cooldown"));
        for (String key : permissionsConfig.getKeys(true)) {
            if (key.endsWith(".cooldown")) {
                int lastDotIndex = key.lastIndexOf(".");
                String parentKey = key.substring(0, lastDotIndex);
                parentKey = parentKey.substring("permissions.".length());

                if (player.hasPermission(parentKey)) {
                    int cooldownInt = permissionsConfig.getInt(key);
                    cooldown.add(cooldownInt);
                }
            }
        }

        return Collections.min(cooldown);
    }

}