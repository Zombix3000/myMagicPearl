package me.zombix.myMagicPearl.Commands;

import me.zombix.myMagicPearl.Managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetPearlCommand implements CommandExecutor {
    private static String badItem;
    private static String successfullySetPearl;

    public SetPearlCommand() {
        reloadValues();
    }

    public static void reloadValues() {
        FileConfiguration messagesConfig = ConfigManager.getMessagesConfig();
        badItem = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("bad-item"));
        successfullySetPearl = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("successfully-set-pearl"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack pearl = player.getItemInHand();

        if (pearl != null && pearl.getType() == Material.ENDER_PEARL) {
            FileConfiguration mainConfig = ConfigManager.getMainConfig();
            mainConfig.set("pearl.meta-data", pearl);
            ConfigManager.saveMainConfig();

            if (!successfullySetPearl.isEmpty()) {
                player.sendMessage(successfullySetPearl.replace("{player}", player.getName()));
            }
        } else {
            if (!badItem.isEmpty()) {
                player.sendMessage(badItem.replace("{player}", player.getName()));
            }
        }

        return true;
    }
}
