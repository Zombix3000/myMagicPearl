package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Actions.GivePlayerPearl;
import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.Bukkit;
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
    private final String playerNotOnline;

    public GivePearlCommand(ConfigManager configManager, GivePlayerPearl givePlayerPearl) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();

        this.configManager = configManager;
        this.givePlayerPearl = givePlayerPearl;
        this.successfullyGive = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("successfully-give"));
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        this.badSender = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("bad-sender"));
        this.playerNotOnline = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("player-not-online"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("mymagicpearl.givepearl")) {
                if (args.length == 0) {
                    if (player.isOnline() && !player.isDead()) {
                        givePlayerPearl.givePearl(player);

                        player.sendMessage(successfullyGive.replace("{player}", player.getName()));
                    }
                } else {
                    Player recipient = Bukkit.getPlayerExact(args[0]);

                    if (recipient != null && recipient.isOnline() && !recipient.isDead()) {
                        givePlayerPearl.givePearl(recipient);

                        player.sendMessage(successfullyGive.replace("{player}", recipient.getName()));
                    } else {
                        player.sendMessage(playerNotOnline.replace("{player}", args[0]));
                    }
                }
            } else {
                player.sendMessage(noPermission.replace("{player}", player.getName()));
            }
        } else {
            if (args.length > 0) {
                Player recipient = Bukkit.getPlayerExact(args[0]);

                if (recipient != null && recipient.isOnline() && !recipient.isDead()) {
                    givePlayerPearl.givePearl(recipient);

                    sender.sendMessage(successfullyGive);
                } else {
                    sender.sendMessage(playerNotOnline);
                }
            } else {
                sender.sendMessage(badSender);
            }
        }
        return true;
    }

}