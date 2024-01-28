package me.zombix.mymagicpearl;

import me.zombix.mymagicpearl.Actions.BlockPearlInteract;
import me.zombix.mymagicpearl.Actions.GivePlayerPearl;
import me.zombix.mymagicpearl.Actions.OnPlayerInteractPearl;
import me.zombix.mymagicpearl.Commands.GivePearlCommand;
import me.zombix.mymagicpearl.Commands.MyMagicPearlCommand;
import me.zombix.mymagicpearl.Commands.SetPearlLobbyCommand;
import me.zombix.mymagicpearl.Config.CommandsTabCompleter;
import me.zombix.mymagicpearl.Config.ConfigManager;
import me.zombix.mymagicpearl.Config.Updates;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyMagicPearl extends JavaPlugin {

    private ConfigManager configManager;
    private Updates updates;
    private GivePlayerPearl givePlayerPearl;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.setupConfig();
        givePlayerPearl = new GivePlayerPearl(this, configManager);

        getLogger().info("Plugin myMagicPearl has been enabled!");

        registerCommands();

        registerEvents();

        for (Player player : getServer().getOnlinePlayers()) {
            givePlayerPearl.givePearl(player);
        }

        if (configManager.getMainConfig().getBoolean("check-for-updates")) {
            getLogger().info("Checking for updates...");
            checkForUpdates();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin myMagicPearl has been disabled!");
    }

    private void registerCommands() {
        CommandExecutor myMagicPearlCommand = new MyMagicPearlCommand(this, configManager, updates, givePlayerPearl);
        CommandExecutor givePearlCommand = new GivePearlCommand(configManager, givePlayerPearl);
        CommandExecutor setPearlLobbyCommand = new SetPearlLobbyCommand(configManager);
        TabCompleter commandsTabCompleter = new CommandsTabCompleter();

        getCommand("mymagicpearl").setExecutor(myMagicPearlCommand);
        getCommand("givepearl").setExecutor(givePearlCommand);
        getCommand("setpearllobby").setExecutor(setPearlLobbyCommand);
        getCommand("mymagicpearl").setTabCompleter(commandsTabCompleter);
    }

    private void checkForUpdates() {
        String pluginName = "myMagicPearl";
        String currentVersion = "v" + getDescription().getVersion();
        String owner = "Zombix3000";
        String repository = "myMagicPearl";

        Updates updates = new Updates(pluginName, currentVersion, owner, repository, this);

        if (updates.checkForUpdates()) {
            getLogger().warning("A new version of the plugin is available! (Current: " + getDescription().getVersion() + ", Latest: " + updates.getLatestVersion() + ")");
        } else {
            getLogger().info("The current version of the plugin is the latest.");
        }
    }

    private void registerEvents() {
        Listener givePlayerPearl = new GivePlayerPearl(this, configManager);
        Listener blockPearlInteract = new BlockPearlInteract(configManager);
        Listener onPlayerThrowPearl = new OnPlayerInteractPearl(this, configManager);

        getServer().getPluginManager().registerEvents(givePlayerPearl, this);
        getServer().getPluginManager().registerEvents(blockPearlInteract, this);
        getServer().getPluginManager().registerEvents(onPlayerThrowPearl, this);
    }

}
