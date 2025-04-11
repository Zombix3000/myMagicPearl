package me.zombix.myMagicPearl.Actions;

import me.zombix.myMagicPearl.Managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static org.bukkit.Bukkit.getServer;

public class GivePearl {
    private static String playerNotOnline;
    private static String slotIsBusy;

    public GivePearl() {
        reloadValues();
    }

    public static void reloadValues() {
        FileConfiguration messagesConfig = ConfigManager.getMessagesConfig();

        playerNotOnline = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("player-not-online"));
        slotIsBusy = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("slot-is-busy"));
    }

    public static void givePearl(CommandSender sender, String playerName) {
        if (playerName != null && !playerName.isEmpty()) {
            Player player = Bukkit.getPlayerExact(playerName);

            if (player != null && player.isOnline()) {
                giveItem(player, sender);
            } else {
                sendConfigMessage(sender, sender instanceof Player, playerNotOnline);
            }
        } else {
            Player player = (Player) sender;
            giveItem(player, sender);
        }
    }

    public static void giveAll() {
        for (Player player : getServer().getOnlinePlayers()) {
            FileConfiguration mainConfig = ConfigManager.getMainConfig();

            int slot = mainConfig.getInt("pearl" + "." + "slot");
            if (slot >= 0 && slot < player.getInventory().getSize()) {
                if (player.getInventory().getItem(slot) == null || mainConfig.getBoolean("replace-item")) {
                    ItemStack magicPearl = new ItemStack(Material.ENDER_PEARL);
                    ItemMeta defaultMeta = magicPearl.getItemMeta();
                    String displayName = ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl" + "." + "display-name"));
                    defaultMeta.setDisplayName(displayName);
                    magicPearl.setItemMeta(defaultMeta);

                    ItemStack itemStackMeta = mainConfig.getItemStack("pearl.meta-data");
                    if (itemStackMeta != null) {
                        ItemMeta itemMeta = itemStackMeta.getItemMeta();
                        magicPearl.setItemMeta(itemMeta);
                    }

                    player.getInventory().setItem(slot, magicPearl);
                }
            }
        }
    }

    private static void giveItem(Player player, CommandSender sender) {
        FileConfiguration mainConfig = ConfigManager.getMainConfig();

        int slot = mainConfig.getInt("pearl" + "." + "slot");
        if (slot >= 0 && slot < player.getInventory().getSize()) {
            if (player.getInventory().getItem(slot) != null && !mainConfig.getBoolean("replace-item")) {
                sendConfigMessage(sender, sender instanceof Player, slotIsBusy);
            } else {
                ItemStack magicPearl = new ItemStack(Material.ENDER_PEARL);
                ItemMeta defaultMeta = magicPearl.getItemMeta();
                String displayName = ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl" + "." + "display-name"));
                defaultMeta.setDisplayName(displayName);
                magicPearl.setItemMeta(defaultMeta);

                ItemStack itemStackMeta = mainConfig.getItemStack("pearl.meta-data");
                if (itemStackMeta != null) {
                    ItemMeta itemMeta = itemStackMeta.getItemMeta();
                    magicPearl.setItemMeta(itemMeta);
                }

                player.getInventory().setItem(slot, magicPearl);
            }
        }
    }

    private static void sendConfigMessage(CommandSender sender, Boolean player, String message) {
        if (!message.isEmpty()) {
            if (player) {
                sender.sendMessage(message.replace("{player}", sender.getName()));
            } else {
                sender.sendMessage(message);
            }
        }
    }

}
