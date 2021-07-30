package me.draimgoose.draimmenu.guiblocks;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class BlocksTabComplete implements TabCompleter {
    DraimMenu plugin;
    public BlocksTabComplete(DraimMenu pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player){
            Player p = ((Player) sender).getPlayer();
            if(args.length == 2) {
                if(args[0].equals("add") && p.hasPermission("draimmenu.block.add")) {
                    ArrayList<String> apanels = new ArrayList<String>(); //all panels
                    try {
                        for (GUI gui : plugin.guiList) {
                            if (!gui.getName().startsWith(args[1])) {
                                continue;
                            }
                            if (sender.hasPermission("draimmenu.gui." + gui.getConfig().getString("perm"))) {
                                if(plugin.guiPerms.isGUIWorldEnabled(p,gui.getConfig())){
                                    apanels.add(gui.getName());
                                }
                            }
                        }
                    } catch (Exception fail) {
                    }
                    return apanels;
                }
            }
            if(args.length == 1){
                ArrayList<String> output = new ArrayList<String>();
                if (sender.hasPermission("draimmenu.block.add")){
                    output.add("add");
                }
                if (sender.hasPermission("draimmenu.block.remove")){
                    output.add("remove");
                }
                if (sender.hasPermission("draimmenu.block.list")){
                    output.add("list");
                }
                return output;
            }
        }
        return null;
    }
}
