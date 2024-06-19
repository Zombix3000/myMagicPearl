package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Actions.GivePlayerPearl;
import me.zombix.mymagicpearl.Config.ConfigManager;
import me.zombix.mymagicpearl.Config.Updates;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public class MyMagicPearlCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Updates updates;
    private final GivePlayerPearl givePlayerPearl;
    private final String noPermission;
    private final Sound actionFailedSound;

    public MyMagicPearlCommand(JavaPlugin plugin, ConfigManager configManager, Updates updates, GivePlayerPearl givePlayerPearl) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();
        FileConfiguration mainConfig = configManager.getMainConfig();

        this.plugin = plugin;
        this.givePlayerPearl = givePlayerPearl;
        this.configManager = configManager;
        this.updates = updates;
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        this.actionFailedSound = Sound.valueOf(mainConfig.getString("action-failed-sound.sound"));
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
                if (args.length > 1) {
                    GivePearlCommand givePearlCommand = new GivePearlCommand(configManager, givePlayerPearl);
                    return givePearlCommand.onCommand(sender, command, label, new String[]{args[1]});
                } else {
                    GivePearlCommand givePearlCommand = new GivePearlCommand(configManager, givePlayerPearl);
                    return givePearlCommand.onCommand(sender, command, label, args);
                }
            } else if (subCommand.equalsIgnoreCase("setpearllobby")) {
                SetPearlLobbyCommand setPearlLobbyCommand = new SetPearlLobbyCommand(configManager);
                return setPearlLobbyCommand.onCommand(sender, command, label, args);
            } else if (subCommand.equalsIgnoreCase("permission")) {
                if (args.length > 3) {
                    String subCommand2 = args[1];

                    if (subCommand2.equalsIgnoreCase("add")) {
                        AddPermissionCommand addPermissionCommand = new AddPermissionCommand(configManager);
                        return addPermissionCommand.onCommand(sender, command, label, args);
                    } else if (subCommand2.equalsIgnoreCase("edit")) {
                        EditPermissionCommand editPermissionCommand = new EditPermissionCommand(configManager);
                        return editPermissionCommand.onCommand(sender, command, label, args);
                    } else if (subCommand2.equalsIgnoreCase("delete")) {
                        DeletePermissionCommand deletePermissionCommand = new DeletePermissionCommand(configManager);
                        return deletePermissionCommand.onCommand(sender, command, label, args);
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            sender.sendMessage(noPermission.replace("{sender}", sender.getName()));
            if (sender instanceof Player) {
                Player player = (Player) sender;
                FileConfiguration mainConfig = configManager.getMainConfig();
                if (mainConfig.getBoolean("action-failed-sound.enabled")) {
                    player.playSound(player.getLocation(), actionFailedSound, 1.0f, 1.0f);
                }
            }
            return true;
        }
        return false;
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
            if (sender.hasPermission("mymagicpearl.managepermissions")) {
                subCommands.add("permission");
            }

            subCommands.add("givepearl");

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(enteredCommand)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String SubCommand = args[1].toLowerCase();

            List<String> subCommands = new ArrayList<>();

            if (SubCommand.equals("permission")) {
                getLogger().info("perm");
                if (sender.hasPermission("mymagicpearl.managepermissions")) {
                    subCommands.add("add");
                    subCommands.add("edit");
                    subCommands.add("delete");
                }
            }

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(SubCommand)) {
                    completions.add(subCommand);
                }
            }
        }

        completions.replaceAll(completion -> completion.replaceFirst("^mymagicpearl:", ""));

        completions.sort(String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

}
