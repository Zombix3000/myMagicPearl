package me.zombix.myMagicPearl;

import me.zombix.myMagicPearl.Actions.GivePearl;
import me.zombix.myMagicPearl.Commands.GivePearlCommand;
import me.zombix.myMagicPearl.Commands.MyMagicPearlCommand;
import me.zombix.myMagicPearl.Commands.SetPearlLobbyCommand;
import me.zombix.myMagicPearl.Listeners.PearlListener;
import me.zombix.myMagicPearl.Managers.CommandsTabCompleteManager;
import me.zombix.myMagicPearl.Managers.ConfigManager;
import me.zombix.myMagicPearl.Managers.UpdatesManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyMagicPearl extends JavaPlugin {
    @Override
    public void onEnable() {
        new ConfigManager(this);
        ConfigManager.loadConfig();

        registerCommands();
        registerEvents();
        GivePearl.giveAll();

        getLogger().info("Plugin myMagicPearl has been enabled!");

        if (ConfigManager.getMainConfig().getBoolean("check-for-updates")) {
            getLogger().info("Checking for updates...");
            checkForUpdates();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin myMagicPearl has been disabled!");
    }

    private void registerCommands() {
        CommandExecutor myMagicPearlCommand = new MyMagicPearlCommand(this);
        CommandExecutor givePearlCommand = new GivePearlCommand();
        CommandExecutor setPearlLobbyCommand = new SetPearlLobbyCommand();
        TabCompleter commandsTabCompleter = new CommandsTabCompleteManager();

        getCommand("mymagicpearl").setExecutor(myMagicPearlCommand);
        getCommand("givepearl").setExecutor(givePearlCommand);
        getCommand("setpearllobby").setExecutor(setPearlLobbyCommand);
        getCommand("mymagicpearl").setTabCompleter(commandsTabCompleter);
    }

    private void registerEvents() {
        Listener pearlListener = new PearlListener(this);

        getServer().getPluginManager().registerEvents(pearlListener, this);
    }

    private void checkForUpdates() {
        String pluginName = "myMagicPearl";
        String currentVersion = "v" + getDescription().getVersion();
        String owner = "Zombix3000";
        String repository = "myMagicPearl";

        UpdatesManager updatesManager = new UpdatesManager(pluginName, currentVersion, owner, repository, this);

        if (updatesManager.checkForUpdates()) {
            getLogger().warning("A new version of the plugin is available! (Current: " + getDescription().getVersion() + ", Latest: " + updatesManager.getLatestVersion() + ")");
            if (ConfigManager.getMainConfig().getBoolean("auto-update")) {
                getLogger().warning("Plugin autoupdate will start in 10 seconds!");
                Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                    @Override
                    public void run() {
                        getLogger().info("Updating plugin...");
                        updatesManager.updatePlugin(null);
                    }
                }, 200L);
            }
        } else {
            getLogger().info("The current version of the plugin is the latest.");
        }
    }

}
