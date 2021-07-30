package me.draimgoose.draimmenu.commandtags.tags.other;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.commandtags.CommandTagEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class DataTags implements Listener {
    DraimMenu plugin;
    public DataTags(DraimMenu pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("set-data=")){
            e.commandTagUsed();
            if(e.args.length == 3){
                plugin.guiData.setUserData(getOffline(e.args[2]),e.args[0],plugin.tex.placeholdersNoColour(e.gui,e.pos,e.p,e.args[1]),true);
                return;
            }
            plugin.guiData.setUserData(e.p.getUniqueId(),e.args[0],plugin.tex.placeholdersNoColour(e.gui,e.pos,e.p,e.args[1]),true);
            return;
        }
        if(e.name.equalsIgnoreCase("add-data=")){
            e.commandTagUsed();
            if(e.args.length == 3){
                plugin.guiData.setUserData(getOffline(e.args[2]),e.args[0],plugin.tex.placeholdersNoColour(e.gui,e.pos,e.p,e.args[1]),false);
                return;
            }
            plugin.guiData.setUserData(e.p.getUniqueId(),e.args[0],plugin.tex.placeholdersNoColour(e.gui,e.pos,e.p,e.args[1]),false);
            return;
        }
        if(e.name.equalsIgnoreCase("math-data=")){
            e.commandTagUsed();
            if(e.args.length == 3){
                plugin.guiData.doDataMath(getOffline(e.args[2]),e.args[0],plugin.tex.placeholdersNoColour(e.gui,e.pos,e.p,e.args[1]));
                return;
            }
            plugin.guiData.doDataMath(e.p.getUniqueId(),e.args[0],plugin.tex.placeholdersNoColour(e.gui,e.pos,e.p,e.args[1]));
            return;
        }
        if(e.name.equalsIgnoreCase("clear-data=")){
            e.commandTagUsed();
            plugin.guiData.clearData(e.p.getUniqueId());
            return;
        }
        if(e.name.equalsIgnoreCase("del-data=")){
            e.commandTagUsed();
            if(e.args.length == 3){
                plugin.guiData.delUserData(getOffline(e.args[1]),e.args[0]);
                return;
            }
            //this will remove data. del-data= [data point] [optional player]
            plugin.guiData.delUserData(e.p.getUniqueId(),e.args[0]);
        }
    }

    @SuppressWarnings("deprecation")
    private UUID getOffline(String playerName){
        return Bukkit.getOfflinePlayer(playerName).getUniqueId();
    }
}
