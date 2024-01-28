package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class ReloadCommand implements CommandExecutor {

    private final ConfigManager configManager;
    private final String successfullyReloaded;
    private final String noPermission;

    public ReloadCommand(ConfigManager configManager) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();

        this.configManager = configManager;
        this.successfullyReloaded = ChatColor.translateAlternateColorCodes('&', "&aPlugin myMagicPearl has been reloaded!");
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("mymagicpearl.reload")) {
            configManager.setupConfig();

            sender.sendMessage(successfullyReloaded);
        } else {
            sender.sendMessage(noPermission.replace("{sender}", sender.getName()));
        }
        return true;
    }
}
