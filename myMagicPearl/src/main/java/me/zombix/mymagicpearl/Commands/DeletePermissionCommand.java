package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class DeletePermissionCommand implements CommandExecutor {
    private final ConfigManager configManager;
    private final String noPermission;
    private final String successfullyDeletePermission;
    private final String isNotPermission;

    public DeletePermissionCommand(ConfigManager configManager) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();

        this.configManager = configManager;
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        this.successfullyDeletePermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("deleted-permission"));
        this.isNotPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("is-not-permission"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("mymagicpearl.managepermissions")) {
            FileConfiguration permissionsConfig = configManager.getPermissionsConfig();

            String permission;
            if (args.length > 2) {
                permission = args[2];
            } else {
                return false;
            }

            if (!permissionsConfig.contains("permissions" + "." + permission)) {
                sender.sendMessage(isNotPermission);
                return true;
            }

            permissionsConfig.set("permissions" + "." + permission, null);

            configManager.savePermissionsConfig();

            sender.sendMessage(successfullyDeletePermission.replace("{permission}", permission));
        } else {
            sender.sendMessage(noPermission);
        }
        return true;
    }
}
