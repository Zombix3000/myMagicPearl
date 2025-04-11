package me.zombix.myMagicPearl.Managers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandsTabCompleteManager implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String enteredCommand = args[0].toLowerCase();
            List<String> subCommands = new ArrayList<>();

            if (command.getName().toLowerCase().equals("mymagicpearl")) {
                if (sender.hasPermission("mymagicpearl.admin")) {
                    subCommands.add("givepearl");
                    subCommands.add("setpearllobby");
                    subCommands.add("setpearl");
                    subCommands.add("permission");
                    subCommands.add("reload");
                    subCommands.add("update");
                } else if (sender.hasPermission("mymagicpearl.givepearl")) {
                    subCommands.add("givepearl");
                }
            }

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(enteredCommand)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String enteredCommand = args[1].toLowerCase();
            List<String> subCommands = new ArrayList<>();

            if (args[0].equals("permission")) {
                if (sender.hasPermission("mymagicpearl.admin")) {
                    subCommands.add("add");
                    subCommands.add("delete");
                }
            }

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(enteredCommand)) {
                    completions.add(subCommand);
                }
            }
        }

        return completions;
    }

}
