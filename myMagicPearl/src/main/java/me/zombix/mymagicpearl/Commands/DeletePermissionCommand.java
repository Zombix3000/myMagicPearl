package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DeletePermissionCommand implements CommandExecutor {
    private final ConfigManager configManager;
    private final String noPermission;
    private final String successfullyDeletePermission;
    private final String isNotPermission;
    private final Sound actionFailedSound;

    public DeletePermissionCommand(ConfigManager configManager) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();
        FileConfiguration mainConfig = configManager.getMainConfig();

        this.configManager = configManager;
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        this.successfullyDeletePermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("deleted-permission"));
        this.isNotPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("is-not-permission"));
        this.actionFailedSound = Sound.valueOf(mainConfig.getString("action-failed-sound.sound"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("mymagicpearl.managepermissions")) {
            FileConfiguration permissionsConfig = configManager.getPermissionsConfig();
            FileConfiguration mainConfig = configManager.getMainConfig();

            String permission;
            if (args.length > 2) {
                permission = args[2];
            } else {
                return false;
            }

            if (!permissionsConfig.contains("permissions" + "." + permission)) {
                sender.sendMessage(isNotPermission);
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (mainConfig.getBoolean("action-failed-sound.enabled")) {
                        player.playSound(player.getLocation(), actionFailedSound, 1.0f, 1.0f);
                    }
                }
                return true;
            }

            permissionsConfig.set("permissions" + "." + permission, null);

            configManager.savePermissionsConfig();

            sender.sendMessage(successfullyDeletePermission.replace("{permission}", permission));
        } else {
            sender.sendMessage(noPermission);
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
