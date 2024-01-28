package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Actions.GivePlayerPearl;
import me.zombix.mymagicpearl.Config.ConfigManager;
import me.zombix.mymagicpearl.Config.Updates;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MyMagicPearlCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Updates updates;
    private final GivePlayerPearl givePlayerPearl;
    private final String noPermission;

    public MyMagicPearlCommand(JavaPlugin plugin, ConfigManager configManager, Updates updates, GivePlayerPearl givePlayerPearl) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();

        this.plugin = plugin;
        this.givePlayerPearl = givePlayerPearl;
        this.configManager = configManager;
        this.updates = updates;
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("mymagicpearl.mymagicpearl")) {
            if (args.length == 0) {
                return false;
            }

            String subCommand = args[0];

            if (subCommand.equalsIgnoreCase("reload")) {
                ReloadCommand reloadCommand = new ReloadCommand(configManager);
                return reloadCommand.onCommand(sender, command, label, args);
            } else if (subCommand.equalsIgnoreCase("update")) {
                UpdateCommand updateCommand = new UpdateCommand(configManager, updates, plugin);
                return updateCommand.onCommand(sender, command, label, args);
            } else if (subCommand.equalsIgnoreCase("givepearl")) {
                GivePearlCommand givePearlCommand = new GivePearlCommand(configManager, givePlayerPearl);
                return givePearlCommand.onCommand(sender, command, label, args);
            } else if (subCommand.equalsIgnoreCase("setpearllobby")) {
                SetPearlLobbyCommand setPearlLobbyCommand = new SetPearlLobbyCommand(configManager);
                return setPearlLobbyCommand.onCommand(sender, command, label, args);
            } else {
                return false;
            }
        } else {
            sender.sendMessage(noPermission.replace("{sender}", sender.getName()));
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String enteredCommand = args[0].toLowerCase();

            List<String> subCommands = new ArrayList<>();

            if (sender.hasPermission("mymagicpearl.reload")) {
                subCommands.add("reload");
            }
            if (sender.hasPermission("mymagicpearl.update")) {
                subCommands.add("update");
            }
            if (sender.hasPermission("mymagicpearl.setpearllobby")) {
                subCommands.add("setpearllobby");
            }

            subCommands.add("givepearl");

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(enteredCommand)) {
                    completions.add(subCommand);
                }
            }
        }

        completions.replaceAll(completion -> completion.replaceFirst("^mymagicpearl:", ""));

        completions.sort(String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

}
