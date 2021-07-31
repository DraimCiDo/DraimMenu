package me.draimgoose.draimmenu.commands;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;


public class CommandGUIList implements CommandExecutor {
    DraimMenu plugin;
    public CommandGUIList(DraimMenu pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("dml") || label.equalsIgnoreCase("draimmenulist") || label.equalsIgnoreCase("dmenul")) {
            if (sender.hasPermission("draimmenu.list")) {
                try {
                    if (plugin.guiList == null) {
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Менюшек не найдено!"));
                        return true;
                    }
                }catch(Exception b){
                    sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Менюшек не найдено!"));
                    return true;
                }

                ArrayList<GUI> guis = new ArrayList<>(plugin.guiList);
                int page = 1;
                int skip = 0;
                if(args.length == 1){
                    try {
                        page = Integer.parseInt(args[0]);
                        skip = page*8-8;
                    }catch (Exception e){
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Недоступная страница"));
                    }
                }
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.DARK_AQUA + "Меню: (Страница " + page + ")"));
                for (int f = skip; guis.size() > f && skip+8 > f; f++) {
                    sender.sendMessage(ChatColor.DARK_GREEN + guis.get(f).getFile().getAbsolutePath().replace(plugin.guiSF.getAbsolutePath(),"") + ChatColor.GREEN + " " + guis.get(f).getName());
                    if(guis.size()-1 == f){
                        return true;
                    }
                }
                sender.sendMessage(ChatColor.AQUA + "Пропишите /dml " + (page+1) + " чтобы прочитать следующую страницу");
            }else{
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Используйте: /dml"));
        return true;
    }
}
