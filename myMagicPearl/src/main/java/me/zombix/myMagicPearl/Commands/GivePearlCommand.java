package me.zombix.myMagicPearl.Commands;

import me.zombix.myMagicPearl.Actions.GivePearl;
import me.zombix.myMagicPearl.Managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class GivePearlCommand implements CommandExecutor {
    private static String noPermission;
    private static String badSender;

    public GivePearlCommand() {
        reloadValues();
    }

    public static void reloadValues() {
        FileConfiguration messagesConfig = ConfigManager.getMessagesConfig();
        noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        badSender = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("bad-sender"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("mymagicpearl.givepearl")) {
                if (args.length == 0) {
                    GivePearl.givePearl(sender, null);
                } else {
                    GivePearl.givePearl(sender, args[0]);
                }
            } else {
                sendConfigMessage(sender, true, noPermission);
            }
        } else {
            if (args.length == 0) {
                sendConfigMessage(sender, false, badSender);
            } else {
                GivePearl.givePearl(sender, args[0]);
            }
        }

        return true;
    }

    private void sendConfigMessage(CommandSender sender, Boolean player, String message) {
        if (!message.isEmpty()) {
            if (player) {
                sender.sendMessage(message.replace("{player}", sender.getName()));
            } else {
                sender.sendMessage(message);
            }
        }
    }

}
