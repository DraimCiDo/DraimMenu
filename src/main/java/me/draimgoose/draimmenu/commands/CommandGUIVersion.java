package me.draimgoose.draimmenu.commands;

import me.draimgoose.draimmenu.DraimMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

public class CommandGUIVersion implements CommandExecutor {
    DraimMenu plugin;
    public CommandGUIVersion(DraimMenu pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equalsIgnoreCase("dmv") || label.equalsIgnoreCase("draimmenuversion") || label.equalsIgnoreCase("dmenuv")) {
            if(args.length == 0) {
                if (sender.hasPermission("draimmenu.version")) {
                    sender.sendMessage(plugin.tex.colour(plugin.tag));
                    sender.sendMessage(ChatColor.GREEN + "Текущая версия " + ChatColor.GRAY + plugin.getDescription().getVersion());
                    sender.sendMessage(ChatColor.GRAY + "-------------------");
                    sender.sendMessage(ChatColor.GREEN + "Разработчик " + ChatColor.GRAY + "DraimGooSe");
                    sender.sendMessage(ChatColor.GREEN + "Команда " + ChatColor.GRAY + "/dm");
                } else {
                    sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                }
            }
        }
        return true;
    }
}
