package me.zombix.myMagicPearl.Commands;

import me.zombix.myMagicPearl.Actions.GivePearl;
import me.zombix.myMagicPearl.Listeners.PearlListener;
import me.zombix.myMagicPearl.Managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    public ReloadCommand() {}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager.loadConfig();
        MyMagicPearlCommand.reloadValues();
        GivePearlCommand.reloadValues();
        GivePearl.reloadValues();
        SetPearlCommand.reloadValues();
        PearlListener.reloadValues();
        SetPearlLobbyCommand.reloadValues();
        PermissionCommand.reloadValues();

        sender.sendMessage(ChatColor.GREEN + "Plugin myMagicPearl has been reloaded!");
        return true;
    }

}
