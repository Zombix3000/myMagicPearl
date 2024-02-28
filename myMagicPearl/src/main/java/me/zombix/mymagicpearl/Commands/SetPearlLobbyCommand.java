package me.zombix.mymagicpearl.Commands;

import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
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
    private final Sound actionFailedSound;

    public SetPearlLobbyCommand(ConfigManager configManager) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();
        FileConfiguration mainConfig = configManager.getMainConfig();

        this.configManager = configManager;
        this.successfullySet = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("successfully-set-lobby"));
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        this.badSender = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("bad-sender"));
        this.actionFailedSound = Sound.valueOf(mainConfig.getString("action-failed-sound.sound"));
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
                    if (mainConfig.getBoolean("action-failed-sound.enabled")) {
                        player.playSound(player.getLocation(), actionFailedSound, 1.0f, 1.0f);
                    }
                }
            }
        } else {
            sender.sendMessage(badSender);
        }
        return true;
    }
}
