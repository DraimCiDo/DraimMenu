package me.draimgoose.draimmenu.completetabs;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class DMTabComplete implements TabCompleter {
    DraimMenu plugin;
    public DMTabComplete(DraimMenu pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player && args.length == 1){
            Player p = ((Player) sender).getPlayer();
            if(label.equalsIgnoreCase("dm") || label.equalsIgnoreCase("dmenu") || label.equalsIgnoreCase("draimmenu")){
                ArrayList<String> agui = new ArrayList<String>();
                for(GUI gui : plugin.guiList) {
                    try {
                        if (!gui.getName().startsWith(args[0])) {
                            continue;
                        }
                        if (sender.hasPermission("draimmenu.gui." + gui.getConfig().getString("perm"))) {
                            if(gui.getConfig().contains("guiType")) {
                                if (gui.getConfig().getStringList("guiType").contains("nocommand")) {
                                    continue;
                                }
                            }
                            if(plugin.guiPerms.isGUIWorldEnabled(p,gui.getConfig())){
                                agui.add(gui.getName());
                            }
                        }
                    }catch(Exception skip){
                    }
                }
                return agui;
            }
        }
        return null;
    }
}
