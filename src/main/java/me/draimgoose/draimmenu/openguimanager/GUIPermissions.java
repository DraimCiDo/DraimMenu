package me.draimgoose.draimmenu.openguimanager;

import me.draimgoose.draimmenu.DraimMenu;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class GUIPermissions {
    DraimMenu plugin;
    public GUIPermissions(DraimMenu pl) {
        this.plugin = pl;
    }

    //если на gui включен мир
    public boolean isGUIWorldEnabled(Player p, ConfigurationSection guiConfig){
        if(guiConfig.contains("disabled-worlds")){
            return !guiConfig.getStringList("disabled-worlds").contains(p.getWorld().getName());
        }
        if(guiConfig.contains("enabled-worlds")){
            return guiConfig.getStringList("enabled-worlds").contains(p.getWorld().getName());
        }
        return true;
    }
}
