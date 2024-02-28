package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {

    private final ConfigManager configManager;
    private final String successfullyReloaded;
    private final String noPermission;
    private final Sound actionFailedSound;

    public ReloadCommand(ConfigManager configManager) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();
        FileConfiguration mainConfig = configManager.getMainConfig();

        this.configManager = configManager;
        this.successfullyReloaded = ChatColor.translateAlternateColorCodes('&', "&aPlugin myMagicPearl has been reloaded!");
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        this.actionFailedSound = Sound.valueOf(mainConfig.getString("action-failed-sound.sound"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("mymagicpearl.reload")) {
            configManager.setupConfig();

            sender.sendMessage(successfullyReloaded);
        } else {
            sender.sendMessage(noPermission.replace("{sender}", sender.getName()));
            if (sender instanceof Player) {
                Player player = (Player) sender;
                FileConfiguration mainConfig = configManager.getMainConfig();
                if (mainConfig.getBoolean("action-failed-sound.enabled")) {
                    player.playSound(player.getLocation(), actionFailedSound, 1.0f, 1.0f);
                }
            }
        }
        return true;
    }
}
