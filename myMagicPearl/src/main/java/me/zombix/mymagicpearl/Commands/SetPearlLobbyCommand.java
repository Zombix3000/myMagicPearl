package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SetPearlLobbyCommand implements CommandExecutor {
    private final ConfigManager configManager;
    private final String successfullySet;
    private final String noPermission;
    private final String badSender;

    public SetPearlLobbyCommand(ConfigManager configManager) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();

        this.configManager = configManager;
        this.successfullySet = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("successfully-set-lobby"));
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        this.badSender = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("bad-sender"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            FileConfiguration mainConfig = configManager.getMainConfig();

            if (mainConfig.getBoolean("lobby-enabled")) {
                if (player.hasPermission("mymagicpearl.setpearllobby")) {

                    Location lobbyLocation = player.getLocation();

                    String worldName = lobbyLocation.getWorld().getName();
                    double x = lobbyLocation.getX();
                    double y = lobbyLocation.getY();
                    double z = lobbyLocation.getZ();
                    float yaw = lobbyLocation.getYaw();
                    float pitch = lobbyLocation.getPitch();

                    mainConfig.set("lobby" + "." + "location" + "." + "x", x);
                    mainConfig.set("lobby" + "." + "location" + "." + "y", y);
                    mainConfig.set("lobby" + "." + "location" + "." + "z", z);
                    mainConfig.set("lobby" + "." + "location" + "." + "world", worldName);
                    mainConfig.set("lobby" + "." + "location" + "." + "yaw", yaw);
                    mainConfig.set("lobby" + "." + "location" + "." + "pitch", pitch);

                    configManager.saveMainConfig();

                    player.sendMessage(successfullySet.replace("{palyer}", sender.getName()));
                } else {
                    player.sendMessage(noPermission.replace("{palyer}", sender.getName()));
                }
            }
        } else {
            sender.sendMessage(badSender);
        }
        return true;
    }
}
