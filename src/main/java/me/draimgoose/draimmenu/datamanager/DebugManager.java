package me.draimgoose.draimmenu.datamanager;

import me.draimgoose.draimmenu.DraimMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class DebugManager {
    DraimMenu plugin;
    public DebugManager(DraimMenu pl) { this.plugin = pl; }

    public HashSet<Player> debugSet = new HashSet<>();
    public boolean consoleDebug = false;

    public boolean isEnabled(Player p){
        return debugSet.contains(p);
    }

    public boolean isEnabled(CommandSender sender){
        if(sender instanceof Player){
            Player p = (Player)sender;
            return isEnabled(p);
        }
        return consoleDebug;
    }
}
