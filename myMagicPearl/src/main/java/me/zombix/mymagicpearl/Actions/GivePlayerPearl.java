package me.zombix.mymagicpearl.Actions;

import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class GivePlayerPearl implements Listener {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final String slotIsBusy;

    public GivePlayerPearl(JavaPlugin plugin, ConfigManager configManager) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();

        this.plugin = plugin;
        this.configManager = configManager;
        this.slotIsBusy = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("slot-is-busy"));
    }

    public void givePearl(Player player) {
        FileConfiguration mainConfig = configManager.getMainConfig();

        int slot = mainConfig.getInt("pearl" + "." + "slot");
        if (slot >= 0 && slot < player.getInventory().getSize()) {
            if (player.getInventory().getItem(slot) != null && !mainConfig.getBoolean("replace-item")) {
                player.sendMessage(slotIsBusy.replace("{player}", player.getName()));
            } else {
                ItemStack magicPearl = new ItemStack(Material.ENDER_PEARL);
                ItemMeta meta = magicPearl.getItemMeta();

                String displayName = ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl" + "." + "display-name"));
                meta.setDisplayName(displayName);

                magicPearl.setItemMeta(meta);

                player.getInventory().setItem(slot, magicPearl);
            }
        }
    }

    public void removePearl(Player player) {
        FileConfiguration mainConfig = configManager.getMainConfig();
        ItemStack magicPearl = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = magicPearl.getItemMeta();

        String displayName = ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl" + "." + "display-name"));

        meta.setDisplayName(displayName);

        magicPearl.setItemMeta(meta);

        player.getInventory().removeItem(magicPearl);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        givePearl(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        givePearl(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        removePearl(player);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        FileConfiguration mainConfig = configManager.getMainConfig();
        ItemStack itemStack = event.getEntity().getItemStack();

        if (itemStack.getType() == Material.ENDER_PEARL && itemStack.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl" + "." + "display-name")))) {
            event.getEntity().remove();
        }
    }
}
