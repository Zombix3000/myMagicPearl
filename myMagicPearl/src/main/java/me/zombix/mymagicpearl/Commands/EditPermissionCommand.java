package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class EditPermissionCommand implements CommandExecutor {
    private final ConfigManager configManager;
    private final String noPermission;
    private final String noInteger;
    private final String successfullyEditPermission;
    private final String isNotPermission;

    public EditPermissionCommand(ConfigManager configManager) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();

        this.configManager = configManager;
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        this.noInteger = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-integer"));
        this.successfullyEditPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("edit-permission"));
        this.isNotPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("is-not-permission"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("mymagicpearl.managepermissions")) {
            FileConfiguration permissionsConfig = configManager.getPermissionsConfig();

            int cooldown;
            String permission;
            if (args.length > 2) {
                try {
                    cooldown = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(noInteger);
                    return true;
                }
                permission = args[2];
            } else {
                return false;
            }

            if (!permissionsConfig.contains("permissions" + "." + permission)) {
                sender.sendMessage(isNotPermission);
                return true;
            }

            permissionsConfig.set("permissions" + "." + permission + "." + "cooldown", cooldown);

            configManager.savePermissionsConfig();

            sender.sendMessage(successfullyEditPermission.replace("{permission}", permission).replace("{cooldown}", String.valueOf(cooldown)));
        } else {
            sender.sendMessage(noPermission);
        }
        return true;
    }
}
