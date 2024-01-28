package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Actions.GivePlayerPearl;
import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class GivePearlCommand implements CommandExecutor {
    private final ConfigManager configManager;
    private final GivePlayerPearl givePlayerPearl;
    private final String successfullyGive;
    private final String noPermission;
    private final String badSender;

    public GivePearlCommand(ConfigManager configManager, GivePlayerPearl givePlayerPearl) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();

        this.configManager = configManager;
        this.givePlayerPearl = givePlayerPearl;
        this.successfullyGive = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("successfully-give"));
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        this.badSender = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("bad-sender"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("mymagicpearl.givepearl")) {
                givePlayerPearl.givePearl(player);

                player.sendMessage(successfullyGive.replace("{player}", sender.getName()));
            } else {
                player.sendMessage(noPermission.replace("{player}", sender.getName()));
            }
        } else {
            sender.sendMessage(badSender);
        }
        return true;
    }
}
