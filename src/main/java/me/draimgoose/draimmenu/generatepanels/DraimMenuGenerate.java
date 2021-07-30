package me.draimgoose.draimmenu.generatepanels;

import me.draimgoose.draimmenu.DraimMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;


public class DraimMenuGenerate implements CommandExecutor {
    DraimMenu plugin;
    public DraimMenuGenerate(DraimMenu pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Пожалуйста, выполните команду как игрок!"));
            return true;
        }
        Player p = (Player) sender;
        if (label.equalsIgnoreCase("dmg") || label.equalsIgnoreCase("draimmenugenerate") || label.equalsIgnoreCase("dmenug")) {
            if (p.hasPermission("draimmenu.generate")) {
                if (args.length == 1) {
                    try {
                        if (Integer.parseInt(args[0]) >= 1 && Integer.parseInt(args[0]) <= 6) {
                            Inventory i = Bukkit.createInventory(null, Integer.parseInt(args[0]) * 9, "Генерация нового меню");
                            p.openInventory(i);
                        } else {
                            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Пожалуйста, используйте целое число от 1 до 6."));
                        }
                    }catch(Exception exc){
                        p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Пожалуйста, используйте целое число от 1 до 6."));
                    }
                    return true;
                }else if (args.length == 0) {
                    if (this.plugin.generateMode.contains(p)) {
                        this.plugin.generateMode.remove(p);
                        p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Режим генерации - Отключен"));
                    } else {
                        this.plugin.generateMode.add(p);
                        p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Режим генерации - Включено"));
                    }
                    return true;
                }
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Используйте: /dmg [линий]"));
                return true;
            }else{
                p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                return true;
            }
        }
        p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Используйте: /dmg [линий]"));
        return true;
    }
}
