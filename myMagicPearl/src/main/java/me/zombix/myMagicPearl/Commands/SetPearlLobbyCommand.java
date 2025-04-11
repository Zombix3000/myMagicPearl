package me.zombix.myMagicPearl.Commands;

import me.zombix.myMagicPearl.Managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SetPearlLobbyCommand implements CommandExecutor {
    private static String successfullySetLobby;

    public SetPearlLobbyCommand() {
        reloadValues();
    }

    public static void reloadValues() {
        FileConfiguration messagesConfig = ConfigManager.getMessagesConfig();
        successfullySetLobby = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("successfully-set-lobby"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        FileConfiguration mainConfig = ConfigManager.getMainConfig();
        Location lobby = player.getLocation();

        mainConfig.set("lobby.location.x", lobby.getX());
        mainConfig.set("lobby.location.y", lobby.getY());
        mainConfig.set("lobby.location.z", lobby.getZ());
        mainConfig.set("lobby.location.world", lobby.getWorld().getName());
        mainConfig.set("lobby.location.yaw", lobby.getYaw());
        mainConfig.set("lobby.location.pitch", lobby.getPitch());
        ConfigManager.saveMainConfig();

        if (!successfullySetLobby.isEmpty()) {
            player.sendMessage(successfullySetLobby.replace("{player}", player.getName()));
        }

        return true;
    }

}
