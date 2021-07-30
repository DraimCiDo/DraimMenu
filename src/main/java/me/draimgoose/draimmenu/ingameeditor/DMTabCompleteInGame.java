package me.draimgoose.draimmenu.ingameeditor;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class DMTabCompleteInGame implements TabCompleter {
    DraimMenu plugin;
    public DMTabCompleteInGame(DraimMenu pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player && args.length == 1){
            Player p = ((Player) sender).getPlayer();
            if(label.equalsIgnoreCase("dme") || label.equalsIgnoreCase("dmenue") || label.equalsIgnoreCase("draimmenuedit")){
                ArrayList<String> apanels = new ArrayList<String>();
                try {
                    for(GUI gui : plugin.guiList) {
                        if(!gui.getName().startsWith(args[0])){
                            continue;
                        }
                        if(sender.hasPermission("draimmenu.gui." + gui.getConfig().getString("perm"))) {
                            if(plugin.guiPerms.isGUIWorldEnabled(p,gui.getConfig())){
                                apanels.add(gui.getName());
                            }
                        }
                    }
                }catch(Exception fail){
                }
                return apanels;
            }
        }
        return null;
    }
}
