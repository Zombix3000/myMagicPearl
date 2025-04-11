package me.zombix.myMagicPearl.Commands;

import me.zombix.myMagicPearl.Managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionCommand implements CommandExecutor {
    private static String addedPermission;
    private static String deletedPermission;

    public PermissionCommand() {
        reloadValues();
    }

    public static void reloadValues() {
        FileConfiguration messagesConfig = ConfigManager.getMessagesConfig();
        addedPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("added-permission"));
        deletedPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("deleted-permission"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration mainConfig = ConfigManager.getMainConfig();
        List<Map<?, ?>> permissionsList = mainConfig.getMapList("permissions");
        String permission = args[2];
        int cooldown;

        if (args[1].equals("add")) {
            if (args.length < 4) {
                return false;
            }

            try {
                cooldown = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                return false;
            }

            Map<String, Integer> newPermission = new HashMap<>();
            newPermission.put(permission, cooldown);

            for (Map<?, ?> map : permissionsList) {
                if (map.containsKey(permission)) {
                    permissionsList.remove(map);
                    break;
                }
            }

            permissionsList.add(newPermission);

            if (!addedPermission.isEmpty()) {
                sender.sendMessage(addedPermission);
            }
        } else if (args[1].equals("delete")) {
            for (Map<?, ?> map : permissionsList) {
                if (map.containsKey(permission)) {
                    permissionsList.remove(map);
                    break;
                }
            }

            if (!deletedPermission.isEmpty()) {
                sender.sendMessage(deletedPermission);
            }
        }

        mainConfig.set("permissions", permissionsList);
        ConfigManager.saveMainConfig();

        return true;
    }

}