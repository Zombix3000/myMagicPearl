package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Config.ConfigManager;
import me.zombix.mymagicpearl.Config.Updates;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Updates updates;
    private final String noPermission;

    public UpdateCommand(ConfigManager configManager, Updates updates, JavaPlugin plugin) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();

        this.plugin = plugin;
        this.configManager = configManager;
        this.updates = updates;
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("mymagicpearl.update")) {
            checkForUpdates(sender);
        } else {
            sender.sendMessage(noPermission.replace("{sender}", sender.getName()));
        }
        return true;
    }

    private void checkForUpdates(CommandSender sender) {
        String pluginName = "myMagicPearl";
        String currentVersion = "v" + plugin.getDescription().getVersion();
        String owner = "Zombix3000";
        String repository = "myMagicPearl";

        Updates updates = new Updates(pluginName, currentVersion, owner, repository, plugin);

        if (updates.checkForUpdates()) {
            updates.updatePlugin();
            sender.sendMessage("Plugin was successfully updated!");
        } else {
            sender.sendMessage("No updates available.");
        }
    }

}
