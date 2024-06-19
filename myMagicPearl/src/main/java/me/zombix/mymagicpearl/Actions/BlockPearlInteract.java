package me.zombix.mymagicpearl.Actions;

import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Bukkit.getLogger;

public class BlockPearlInteract implements Listener {
    private final ConfigManager configManager;
    private final String cannotDoIt;
    private final Sound actionFailedSound;

    public BlockPearlInteract(ConfigManager configManager) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();
        FileConfiguration mainConfig = configManager.getMainConfig();

        this.configManager = configManager;
        this.cannotDoIt = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("cannot-do-it"));
        this.actionFailedSound = Sound.valueOf(mainConfig.getString("action-failed-sound.sound"));
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        FileConfiguration mainConfig = configManager.getMainConfig();

        if (droppedItem != null && droppedItem.getType() == Material.ENDER_PEARL && droppedItem.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl" + "." + "display-name"))) && !mainConfig.getBoolean("allow-drop-pearl")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(cannotDoIt.replace("{player}", event.getPlayer().getName()));
            if (mainConfig.getBoolean("action-failed-sound.enabled")) {
                event.getPlayer().playSound(event.getPlayer().getLocation(), actionFailedSound, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        FileConfiguration mainConfig = configManager.getMainConfig();
        ItemStack item = event.getCurrentItem();

        if (item != null && item.getType() == Material.ENDER_PEARL && item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl" + "." + "display-name")))) {
            if (event.getClick() == ClickType.CREATIVE && mainConfig.getBoolean("creative-actions")) {
                return;
            } else {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(cannotDoIt);
                if (mainConfig.getBoolean("action-failed-sound.enabled")) {
                    Player player = (Player) event.getWhoClicked();
                    player.playSound(player.getLocation(), actionFailedSound, 1.0f, 1.0f);
                }
            }
        }
    }

}
