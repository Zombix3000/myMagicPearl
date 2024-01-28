package me.zombix.mymagicpearl.Actions;

import me.zombix.mymagicpearl.Config.ConfigManager;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class OnPlayerInteractPearl implements Listener {
    private final ConfigManager configManager;
    private final JavaPlugin plugin;
    private final Map<UUID, EnderPearl> thrownPearls;
    private final Map<String, EnderPearl> thrownMagicPearls;
    private final Map<UUID, Boolean> sneakingPlayers;
    private final Map<UUID, Integer> pearlsCooldown;
    private final Map<UUID, String> isBlocked;
    private final String noPermission;
    private final String notSetLobby;
    private final String successfullyTeleportToLobby;

    public OnPlayerInteractPearl(JavaPlugin plugin, ConfigManager configManager) {
        FileConfiguration messagesConfig = configManager.getMessagesConfig();

        this.plugin = plugin;
        this.configManager = configManager;
        this.thrownPearls = new HashMap<>();
        this.thrownMagicPearls = new HashMap<>();
        this.sneakingPlayers = new HashMap<>();
        this.pearlsCooldown = new HashMap<>();
        this.isBlocked = new HashMap<>();
        this.noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        this.notSetLobby = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("not-set-lobby"));
        this.successfullyTeleportToLobby = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("successfully-teleport-to-lobby"));
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof EnderPearl) {
            EnderPearl enderPearl = (EnderPearl) event.getEntity();
            FileConfiguration mainConfig = configManager.getMainConfig();

            if (enderPearl.getItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl" + "." + "display-name")))) {
                if (enderPearl.getShooter() instanceof Player) {
                    Player player = (Player) enderPearl.getShooter();

                    if (player.hasPermission("mymagicpearl.throwpearl")) {
                        if (!pearlsCooldown.containsKey(player.getUniqueId())) {
                            trackThrownPearl(player, enderPearl);
                        } else {
                            event.setCancelled(true);
                            isBlocked.put(player.getUniqueId(), "Nothing");
                        }
                    } else {
                        player.sendMessage(noPermission.replace("{player}", player.getName()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof EnderPearl) {
            EnderPearl enderPearl = (EnderPearl) event.getDamager();
            Player player = (Player) event.getEntity();
            FileConfiguration mainConfig = configManager.getMainConfig();

            if (!mainConfig.getBoolean("pearl-give-damage")) {
                event.setCancelled(true);
            }

            removeThrownPearl(player, enderPearl);
            doPearlCooldown(player);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.ENDERMITE && isSpawnedByMagicPearl(event)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        sneakingPlayers.put(playerUUID, event.isSneaking());

        if (thrownPearls.containsKey(playerUUID) && event.isSneaking()) {
            EnderPearl enderPearl = thrownPearls.get(playerUUID);

            player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

            thrownPearls.remove(playerUUID);
            removeThrownPearl(player, enderPearl);
            doPearlCooldown(player);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        FileConfiguration mainConfig = configManager.getMainConfig();

        if (droppedItem.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl" + "." + "display-name")))) {
            isBlocked.put(player.getUniqueId(), "Nothing");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        FileConfiguration mainConfig = configManager.getMainConfig();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            //return;
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (isBlocked.containsKey(player.getUniqueId())) {
                isBlocked.remove(player.getUniqueId());
            } else {
                if (player.getInventory().getItemInMainHand().getType() == Material.ENDER_PEARL && player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl" + "." + "display-name")))) {
                    if (player.hasPermission("mymagicpearl.teleporttolobby")) {
                        event.setCancelled(true);

                        double x = mainConfig.getDouble("lobby" + "." + "location" + "." + "x");
                        double y = mainConfig.getDouble("lobby" + "." + "location" + "." + "y");
                        double z = mainConfig.getDouble("lobby" + "." + "location" + "." + "z");
                        String worldName = mainConfig.getString("lobby" + "." + "location" + "." + "world");
                        float yaw = (float) mainConfig.getDouble("lobby" + "." + "location" + "." + "yaw");
                        float pitch = (float) mainConfig.getDouble("lobby" + "." + "location" + "." + "pitch");

                        if (worldName != null) {
                            player.teleport(new Location(player.getServer().getWorld(worldName), x, y, z, yaw, pitch));
                            player.sendMessage(successfullyTeleportToLobby.replace("{player}", player.getName()));
                        } else {
                            player.sendMessage(notSetLobby.replace("{player}", player.getName()));
                        }
                    } else {
                        player.sendMessage(noPermission.replace("{player}", player.getName()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (pearlsCooldown.containsKey(player.getUniqueId())) {
            pearlsCooldown.remove(player.getUniqueId());
        }
    }

    public void trackThrownPearl(Player player, EnderPearl enderPearl) {
        UUID playerUUID = player.getUniqueId();
        thrownPearls.put(playerUUID, enderPearl);
        thrownMagicPearls.put("Nothing", enderPearl);

        enderPearl.setPassenger(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!enderPearl.isValid()) {
                    thrownPearls.remove(playerUUID);
                    cancel();
                    return;
                }

                if (!thrownPearls.containsKey(playerUUID)) {
                    enderPearl.remove();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public void removeThrownPearl(Player player, EnderPearl enderPearl) {
        UUID playerUUID = player.getUniqueId();
        thrownPearls.remove(playerUUID);

        enderPearl.remove();

        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private boolean isSpawnedByMagicPearl(CreatureSpawnEvent event) {
        for (EnderPearl enderPearl : thrownMagicPearls.values()) {
            ProjectileSource shooter = enderPearl.getShooter();
            if (shooter instanceof LivingEntity) {
                LivingEntity livingShooter = (LivingEntity) shooter;
                if (livingShooter.getUniqueId().equals(event.getEntity().getUniqueId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void doPearlCooldown(Player player) {
        FileConfiguration mainConfig = configManager.getMainConfig();
        int cooldown = mainConfig.getInt("pearl" + "." + "cooldown");

        pearlsCooldown.put(player.getUniqueId(), cooldown);
        giveCooldownPearls(player, cooldown);

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (pearlsCooldown.containsKey(player.getUniqueId())) {
                int remaining = pearlsCooldown.getOrDefault(player.getUniqueId(), 1);
                if (remaining <= 1) {
                    pearlsCooldown.remove(player.getUniqueId());
                } else {
                    pearlsCooldown.put(player.getUniqueId(), remaining - 1);
                    giveCooldownPearls(player, remaining - 1);
                }
            }
        }, 20L, 20L);
    }

    private void giveCooldownPearls(Player player, Integer amount) {
        FileConfiguration mainConfig = configManager.getMainConfig();
        int slot = mainConfig.getInt("pearl" + "." + "slot");
        ItemStack magicPearl = new ItemStack(Material.ENDER_PEARL, amount);
        ItemMeta meta = magicPearl.getItemMeta();

        String displayName = ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl" + "." + "display-name"));
        meta.setDisplayName(displayName);

        magicPearl.setItemMeta(meta);

        player.getInventory().setItem(slot, magicPearl);
    }

}
