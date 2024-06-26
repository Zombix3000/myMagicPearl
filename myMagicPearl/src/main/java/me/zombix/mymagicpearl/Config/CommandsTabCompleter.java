package me.zombix.mymagicpearl.Config;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public class CommandsTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String enteredCommand = args[0].toLowerCase();

            List<String> subCommands = new ArrayList<>();

            if (command.getName().toLowerCase().equals("mymagicpearl")) {
                if (sender.hasPermission("mymagicpearl.reload")) {
                    subCommands.add("reload");
                }
                if (sender.hasPermission("mymagicpearl.reload")) {
                    subCommands.add("update");
                }
                if (sender.hasPermission("mymagicpearl.setpearllobby")) {
                    subCommands.add("setpearllobby");
                }
                if (sender.hasPermission("mymagicpearl.managepermissions")) {
                    subCommands.add("permission");
                }

                subCommands.add("givepearl");
            }

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(enteredCommand)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            /*String SubCommand = args[1].toLowerCase();
            getLogger().info("perm");
            getLogger().info(SubCommand);

            List<String> subCommands = new ArrayList<>();

            if (command.getName().toLowerCase().equals("mymagicpearl")) {
                getLogger().info("perm2");
                if (SubCommand.equals("permission")) {
                    getLogger().info("perm3");
                    if (sender.hasPermission("mymagicpearl.managepermissions")) {
                        getLogger().info("perm4");
                        subCommands.add("add");
                        subCommands.add("edit");
                        subCommands.add("delete");
                    }
                }
            }

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(SubCommand)) {
                    completions.add(subCommand);
                }
            }*/
            String enteredCommand = args[1].toLowerCase();

            List<String> subCommands = new ArrayList<>();

            if (enteredCommand.equals("permission")) {
                if (sender.hasPermission("mymagicpearl.reload")) {
                    subCommands.add("reload");
                }
                if (sender.hasPermission("mymagicpearl.reload")) {
                    subCommands.add("update");
                }
                if (sender.hasPermission("mymagicpearl.setpearllobby")) {
                    subCommands.add("setpearllobby");
                }
                if (sender.hasPermission("mymagicpearl.managepermissions")) {
                    subCommands.add("permission");
                }

                subCommands.add("givepearl");
            }

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(enteredCommand)) {
                    completions.add(subCommand);
                }
            }
        }

        completions.replaceAll(completion -> completion.replaceFirst("^mymagicpearl:", ""));

        completions.sort(String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

}
