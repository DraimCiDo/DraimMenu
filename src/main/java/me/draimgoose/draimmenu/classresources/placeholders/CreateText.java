package me.draimgoose.draimmenu.classresources.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateText {
    DraimMenu plugin;
    public CreateText(DraimMenu pl) {
        this.plugin = pl;
    }

    public void sendMessage(GUI gui,GUIPosition position, Player p, String message){
        if(!message.equals("")) {
            p.sendMessage(placeholders(gui,position, p,plugin.tag + message));
        }
    }

    public void sendMessage(Player p, String message){
        if(!message.equals("")) {
            p.sendMessage(colour(plugin.tag + message));
        }
    }

    public void sendString(GUI gui,GUIPosition position, Player p, String message){
        if(!message.equals("")) {
            p.sendMessage(placeholders(gui,position, p,message));
        }
    }

    public void sendString(Player p, String message){
        if(!message.equals("")) {
            p.sendMessage(colour(message));
        }
    }

    public List<String> placeholdersNoColour(GUI gui,GUIPosition position, Player p, List<String> setpapi) {
        try {
            int tempInt = 0;
            for (String temp : setpapi) {
                setpapi.set(tempInt, attachPlaceholders(gui,position, p, temp));
                tempInt += 1;
            }
        }catch(Exception ignore){
            return null;
        }
        return setpapi;
    }

    public List<String> placeholdersList(GUI gui,GUIPosition position, Player p, List<String> setpapi, boolean placeholder) {
        try {
            if(placeholder) {
                int tempInt = 0;
                for (String temp : setpapi) {
                    setpapi.set(tempInt, attachPlaceholders(gui,position, p, temp));
                    tempInt += 1;
                }
            }
        }catch(Exception ignore){}
        int tempInt = 0;
        for(String temp : setpapi){
            try {
                setpapi.set(tempInt, plugin.hex.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', temp)));
            }catch(NullPointerException ignore){}
            tempInt += 1;
        }
        return setpapi;
    }

    public String colour(String setpapi) {
        try {
            setpapi = plugin.hex.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', setpapi));
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }

    public String placeholdersNoColour(GUI gui, GUIPosition position, Player p, String setpapi) {
        try {
            setpapi = attachPlaceholders(gui,position, p,setpapi);
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }

    public String placeholders(GUI gui, GUIPosition position, Player p, String setpapi) {
        try {
            setpapi = attachPlaceholders(gui,position, p,setpapi);
            setpapi = plugin.hex.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', setpapi));
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }

    public String attachPlaceholders(GUI gui, GUIPosition position, Player p, String input){
        input = plugin.placeholders.setPlaceholders(gui,position, p, input, false);
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            OfflinePlayer offp = plugin.getServer().getOfflinePlayer(p.getUniqueId());
            input = PlaceholderAPI.setPlaceholders(offp, input);
        }
        input = plugin.placeholders.setPlaceholders(gui,position, p, input, true);
        return input;
    }
}
