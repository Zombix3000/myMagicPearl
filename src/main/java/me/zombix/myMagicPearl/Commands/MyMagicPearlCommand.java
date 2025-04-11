package me.zombix.myMagicPearl.Commands;

import me.zombix.myMagicPearl.Actions.GivePearl;
import me.zombix.myMagicPearl.Managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class MyMagicPearlCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private static String noPermission;
    private static String badSender;

    public MyMagicPearlCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadValues();
    }

    public static void reloadValues() {
        FileConfiguration messagesConfig = ConfigManager.getMessagesConfig();
        noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        badSender = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("bad-sender"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0];

        if (sender instanceof Player) {
            if (sender.hasPermission("mymagicpearl.admin")) {
                if (subCommand.equalsIgnoreCase("setpearllobby")) {
                    SetPearlLobbyCommand setPearlLobbyCommand = new SetPearlLobbyCommand();
                    return setPearlLobbyCommand.onCommand(sender, command, label, args);
                } else if (subCommand.equalsIgnoreCase("givepearl")) {
                    String[] argsWithoutFirst = Arrays.copyOfRange(args, 1, args.length);
                    GivePearlCommand givePearlCommand = new GivePearlCommand();
                    return givePearlCommand.onCommand(sender, command, label, argsWithoutFirst);
                } else if (subCommand.equalsIgnoreCase("setpearl")) {
                    SetPearlCommand setPearlCommand = new SetPearlCommand();
                    return setPearlCommand.onCommand(sender, command, label, args);
                } else return commandExecute(sender, command, label, args, subCommand);
            } else if (sender.hasPermission("mymagicpearl.givepearl") && subCommand.equalsIgnoreCase("givepearl")) {
                String[] argsWithoutFirst = Arrays.copyOfRange(args, 1, args.length);
                GivePearlCommand givePearlCommand = new GivePearlCommand();
                return givePearlCommand.onCommand(sender, command, label, argsWithoutFirst);
            } else {
                sendConfigMessage(sender, true, noPermission);
                return true;
            }
        } else if (subCommand.equalsIgnoreCase("givepearl")) {
            String[] argsWithoutFirst = Arrays.copyOfRange(args, 1, args.length);
            GivePearlCommand givePearlCommand = new GivePearlCommand();
            return givePearlCommand.onCommand(sender, command, label, argsWithoutFirst);
        } else if (subCommand.equalsIgnoreCase("setpearllobby")) {
            sendConfigMessage(sender, false, badSender);
            return true;
        } else {
            return commandExecute(sender, command, label, args, subCommand);
        }
    }

    private boolean commandExecute(CommandSender sender, Command command, String label, String[] args, String subCommand) {
        if (subCommand.equalsIgnoreCase("reload")) {
            ReloadCommand reloadCommand = new ReloadCommand();
            return reloadCommand.onCommand(sender, command, label, args);
        } else if (subCommand.equalsIgnoreCase("update")) {
            UpdateCommand updateCommand = new UpdateCommand(plugin);
            return updateCommand.onCommand(sender, command, label, args);
        } else if (subCommand.equalsIgnoreCase("permission") && args.length >= 3 && (args[1].equals("add") || args[1].equals("delete"))) {
            PermissionCommand permissionCommand = new PermissionCommand();
            return permissionCommand.onCommand(sender, command, label, args);
        } else {
            return false;
        }
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
