package me.draimgoose.draimmenu.commands;

import me.draimgoose.draimmenu.DraimMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class CommandGUIDebug implements CommandExecutor {
    DraimMenu plugin;
    public CommandGUIDebug(DraimMenu pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("dmd") || label.equalsIgnoreCase("draimmenudebug") || label.equalsIgnoreCase("dmenud")) {
            if (sender.hasPermission("draimmenu.debug")) {
                if (args.length == 0) {
                    if(!(sender instanceof Player)) {
                        plugin.debug.consoleDebug = !plugin.debug.consoleDebug;
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Глобальный режим дебага: " + plugin.debug.consoleDebug));
                        return true;
                    }

                    Player p = (Player)sender;
                    if(plugin.debug.isEnabled(p)){
                        plugin.debug.debugSet.remove(p);
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Персональный режим дебага отключен!"));
                    }else{
                        plugin.debug.debugSet.add(p);
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Персональный режим дебага включен!"));
                    }
                }else{
                    sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Используйте: /dmd"));
                }
            }else{
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Используйте: /dmd"));
        return true;
    }
}
