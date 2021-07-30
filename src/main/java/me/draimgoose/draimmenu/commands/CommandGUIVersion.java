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
                    String latestVersion = plugin.updater.getLatestVersion(false);
                    sender.sendMessage(plugin.tex.colour(plugin.tag));
                    sender.sendMessage(ChatColor.GREEN + "Текущая версия " + ChatColor.GRAY + plugin.getDescription().getVersion());
                    sender.sendMessage(ChatColor.GREEN + "Последняя версия " + ChatColor.GRAY + latestVersion);
                    sender.sendMessage(ChatColor.GRAY + "-------------------");
                    sender.sendMessage(ChatColor.GREEN + "Разработчик " + ChatColor.GRAY + "DraimGooSe");
                    sender.sendMessage(ChatColor.GREEN + "Команда " + ChatColor.GRAY + "/dm");
                } else {
                    sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                }
            }else if(args.length == 1){
                if (sender.hasPermission("draimmenu.update")) {
                    if (args[0].equals("отмена")) {
                        plugin.updater.downloadVersionManually = null;
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Новая версия не будет загружаться при перезапуске."));
                    } else {
                        plugin.updater.downloadVersionManually = args[0];
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Скачивание версии " + ChatColor.GRAY + args[0] + ChatColor.GREEN + " при перезагрузке сервера."));
                    }
                }else{
                    sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                }
            }else{
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Используйте: /dmv [update:latest:отмена]"));
            }
            return true;
        }
        return true;
    }
}
