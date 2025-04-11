package me.zombix.myMagicPearl.Listeners;

import me.zombix.myMagicPearl.Actions.GivePearl;
import me.zombix.myMagicPearl.Managers.ConfigManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PearlListener implements Listener {
    private JavaPlugin plugin;
    private Map<UUID, EnderPearl> thrownPearls;
    private List<EnderPearl> thrownMagicPearls;
    private Map<UUID, Boolean> sneakingPlayers;
    private Map<UUID, Integer> pearlsCooldown;
    private Map<UUID, Integer> playerCooldown;
    private Map<UUID, Boolean> isBlocked;
    private static ItemStack mainEnderPearl;
    private static String noPermission;
    private static String onCooldown;
    private static String successfullyTeleportToLobby;
    private static String notSetLobby;

    public PearlListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.thrownPearls = new HashMap<>();
        this.thrownMagicPearls = new ArrayList<>();
        this.sneakingPlayers = new HashMap<>();
        this.pearlsCooldown = new HashMap<>();
        this.playerCooldown = new HashMap<>();
        this.isBlocked = new HashMap<>();
        reloadValues();
    }

    public static void reloadValues() {
        FileConfiguration mainConfig = ConfigManager.getMainConfig();
        FileConfiguration messagesConfig = ConfigManager.getMessagesConfig();
        ItemStack configItemStack = mainConfig.getItemStack("pearl.meta-data");

        if (configItemStack != null) {
            mainEnderPearl = configItemStack;
        } else {
            ItemStack nameItemStack = new ItemStack(Material.ENDER_PEARL);
            ItemMeta nameItemMeta = nameItemStack.getItemMeta();
            nameItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', mainConfig.getString("pearl.display-name")));
            nameItemStack.setItemMeta(nameItemMeta);
            mainEnderPearl = nameItemStack;
        }
        mainEnderPearl.setAmount(1);

        noPermission = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("no-permission"));
        onCooldown = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("player-on-cooldown"));
        successfullyTeleportToLobby = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("successfully-teleport-to-lobby"));
        notSetLobby = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("not-set-lobby"));
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof EnderPearl) {
            EnderPearl enderPearl = (EnderPearl) event.getEntity();

            if (isMainEnderPearl(enderPearl.getItem())) {
                if (enderPearl.getShooter() instanceof Player) {
                    Player player = (Player) enderPearl.getShooter();

                    if (player.hasPermission("mymagicpearl.throwpearl")) {
                        if (!pearlsCooldown.containsKey(player.getUniqueId())) {
                            trackThrownPearl(player, enderPearl);
                        } else {
                            event.setCancelled(true);
                            isBlocked.put(player.getUniqueId(), true);
                            Integer cooldownValue = playerCooldown.get(player.getUniqueId());
                            if (cooldownValue != null) {
                                sendConfigMessage(player, onCooldown.replace("{cooldown}", cooldownValue.toString()));
                            }
                        }
                    } else {
                        event.setCancelled(true);
                        isBlocked.put(player.getUniqueId(), true);
                        sendConfigMessage(player, noPermission);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        FileConfiguration mainConfig = ConfigManager.getMainConfig();

        if (mainConfig.getBoolean("lobby.enabled")) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (isBlocked.containsKey(player.getUniqueId())) {
                    isBlocked.remove(player.getUniqueId());
                } else {
                    if (isMainEnderPearl(player.getInventory().getItemInMainHand())) {
                        if (player.hasPermission("mymagicpearl.teleporttolobby")) {
                            event.setCancelled(true);

                            double x = mainConfig.getDouble("lobby" + "." + "location" + "." + "x");
                            double y = mainConfig.getDouble("lobby" + "." + "location" + "." + "y");
                            double z = mainConfig.getDouble("lobby" + "." + "location" + "." + "z");
                            World world = null;
                            try {
                                world = player.getServer().getWorld(mainConfig.getString("lobby" + "." + "location" + "." + "world"));
                            } catch (Exception ignored) {}
                            float yaw = (float) mainConfig.getDouble("lobby" + "." + "location" + "." + "yaw");
                            float pitch = (float) mainConfig.getDouble("lobby" + "." + "location" + "." + "pitch");

                            if (world != null) {
                                if (mainConfig.getBoolean("use-yaw-and-pitch")) {
                                    player.teleport(new Location(world, x, y, z, yaw, pitch));
                                } else {
                                    player.teleport(new Location(world, x, y, z));
                                }
                                sendConfigMessage(player, successfullyTeleportToLobby);
                            } else {
                                sendConfigMessage(player, notSetLobby);
                            }
                        } else {
                            sendConfigMessage(player, noPermission);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof EnderPearl && event.getEntity() instanceof Player) {
            EnderPearl enderPearl = (EnderPearl) event.getDamager();

            if (isMainEnderPearl(enderPearl.getItem())) {
                Player player = (Player) event.getEntity();
                FileConfiguration mainConfig = ConfigManager.getMainConfig();

                if (!mainConfig.getBoolean("pearl-give-damage")) {
                    event.setCancelled(true);
                }

                removeThrownPearl(player, enderPearl);
                doPearlCooldown(player);
            }
        }

        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            FileConfiguration mainConfig = ConfigManager.getMainConfig();

            if (mainConfig.getBoolean("lobby.enabled")) {
                if (isBlocked.containsKey(player.getUniqueId())) {
                    isBlocked.remove(player.getUniqueId());
                } else {
                    if (isMainEnderPearl(player.getInventory().getItemInMainHand())) {
                        if (player.hasPermission("mymagicpearl.teleporttolobby")) {
                            event.setCancelled(true);

                            double x = mainConfig.getDouble("lobby" + "." + "location" + "." + "x");
                            double y = mainConfig.getDouble("lobby" + "." + "location" + "." + "y");
                            double z = mainConfig.getDouble("lobby" + "." + "location" + "." + "z");
                            World world = null;
                            try {
                                world = player.getServer().getWorld(mainConfig.getString("lobby" + "." + "location" + "." + "world"));
                            } catch (Exception ignored) {}
                            float yaw = (float) mainConfig.getDouble("lobby" + "." + "location" + "." + "yaw");
                            float pitch = (float) mainConfig.getDouble("lobby" + "." + "location" + "." + "pitch");

                            if (world != null) {
                                if (mainConfig.getBoolean("use-yaw-and-pitch")) {
                                    player.teleport(new Location(world, x, y, z, yaw, pitch));
                                } else {
                                    player.teleport(new Location(world, x, y, z));
                                }
                                sendConfigMessage(player, successfullyTeleportToLobby);
                            } else {
                                sendConfigMessage(player, notSetLobby);
                            }
                        } else {
                            sendConfigMessage(player, noPermission);
                        }
                    }
                }
            }
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
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.ENDERMITE && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.ENDER_PEARL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ConfigManager.getMainConfig().getBoolean("allow-inventory-action")) {
            ItemStack item = event.getCurrentItem();
            if (isMainEnderPearl(item)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!ConfigManager.getMainConfig().getBoolean("allow-inventory-action")) {
            ItemStack item = event.getItemDrop().getItemStack();
            if (isMainEnderPearl(item)) {
                event.setCancelled(true);
                isBlocked.put(event.getPlayer().getUniqueId(), true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!ConfigManager.getMainConfig().getBoolean("allow-inventory-action")) {
            event.getDrops().removeIf(this::isMainEnderPearl);
        }

        pearlsCooldown.remove(event.getEntity().getUniqueId());
        playerCooldown.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        GivePearl.givePearl(event.getPlayer(), null);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        GivePearl.givePearl(event.getPlayer(), null);
    }

    public void trackThrownPearl(Player player, EnderPearl enderPearl) {
        UUID playerUUID = player.getUniqueId();
        FileConfiguration mainConfig = ConfigManager.getMainConfig();
        thrownPearls.put(playerUUID, enderPearl);
        thrownMagicPearls.add(enderPearl);

        enderPearl.setPassenger(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (mainConfig.getBoolean("subtitles.enabled")) {
                    showActionBar(player);
                }

                if (mainConfig.getBoolean("pearl-particles.enabled")) {
                    enderPearl.getWorld().spawnParticle(Particle.valueOf(mainConfig.getString("pearl-particles.particle")), enderPearl.getLocation(), mainConfig.getInt("pearl-particles.intensity"));
                }

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
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
    }

    private void doPearlCooldown(Player player) {
        int cooldown = getCooldownForPermission(player);
        if (cooldown < 1) {
            cooldown = 1;
        } else if (cooldown > 64) {
            cooldown = 64;
        }

        pearlsCooldown.put(player.getUniqueId(), cooldown);
        giveCooldownPearls(player, cooldown);

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (pearlsCooldown.containsKey(player.getUniqueId())) {
                int remaining = pearlsCooldown.getOrDefault(player.getUniqueId(), 1);
                if (remaining <= 1) {
                    pearlsCooldown.remove(player.getUniqueId());
                    playerCooldown.remove(player.getUniqueId());
                } else {
                    pearlsCooldown.put(player.getUniqueId(), remaining - 1);
                    giveCooldownPearls(player, remaining - 1);
                    playerCooldown.put(player.getUniqueId(), remaining - 1);
                }
            }
        }, 20L, 20L);
    }

    private Integer getCooldownForPermission(Player player) {
        FileConfiguration mainConfig = ConfigManager.getMainConfig();
        int defaultCooldown = mainConfig.getInt("pearl.default-cooldown");
        List<Map<?, ?>> tempList = mainConfig.getMapList("permissions");
        List<Map<String, Integer>> permissionsList = new ArrayList<>();

        for (Map<?, ?> map : tempList) {
            Map<String, Integer> typedMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                int value;
                if (entry.getValue() instanceof Integer) {
                    value = (Integer) entry.getValue();
                } else if (entry.getValue() instanceof Number) {
                    value = ((Number) entry.getValue()).intValue();
                } else {
                    try {
                        value = Integer.parseInt(entry.getValue().toString());
                    } catch (NumberFormatException ex) {
                        continue;
                    }
                }
                typedMap.put(key, value);
            }
            permissionsList.add(typedMap);
        }

        int minCooldown = Integer.MAX_VALUE;
        for (Map<String, Integer> permissionEntry : permissionsList) {
            for (Map.Entry<String, Integer> entry : permissionEntry.entrySet()) {
                String permission = entry.getKey();
                int cooldown = entry.getValue();

                if (player.hasPermission(permission)) {
                    if (cooldown < minCooldown) {
                        minCooldown = cooldown;
                    }
                }
            }
        }

        return (minCooldown == Integer.MAX_VALUE) ? defaultCooldown : minCooldown;
    }

    private void giveCooldownPearls(Player player, Integer amount) {
        FileConfiguration mainConfig = ConfigManager.getMainConfig();
        int slot = mainConfig.getInt("pearl" + "." + "slot");
        ItemStack magicPearl = mainEnderPearl.clone();
        magicPearl.setAmount(amount);

        player.getInventory().setItem(slot, magicPearl);
    }

    private void showActionBar(Player player) {
        FileConfiguration mainConfig = ConfigManager.getMainConfig();
        String actionbarText = mainConfig.getString("subtitles.action-bar");

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', actionbarText)));
    }

    private static void sendConfigMessage(Player player, String message) {
        if (!message.isEmpty()) {
            player.sendMessage(message.replace("{player}", player.getName()));
        }
    }

    private boolean isMainEnderPearl(ItemStack item) {
        if (item == null) return false;
        ItemStack clone = item.clone();
        clone.setAmount(1);
        return mainEnderPearl.equals(clone);
    }

}
