package me.draimgoose.draimmenu.generatepanels;

import me.draimgoose.draimmenu.DraimMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class TabCompleteGenerate implements TabCompleter {
    DraimMenu plugin;
    public TabCompleteGenerate(DraimMenu pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player && args.length == 1){
            if(label.equalsIgnoreCase("dmg") || label.equalsIgnoreCase("dmenug") || label.equalsIgnoreCase("draimmenugenerate")){
                if(sender.hasPermission("draimmenu.generate")) {
                    ArrayList<String> agui = new ArrayList<String>();
                    agui.add("1");
                    agui.add("2");
                    agui.add("3");
                    agui.add("4");
                    agui.add("5");
                    agui.add("6");
                    return agui;
                }
            }
        }
        return null;
    }
}
