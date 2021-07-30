package me.draimgoose.draimmenu.commandtags;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CommandTagEvent extends Event {

    public final Player p;
    public final GUI gui;
    public String[] raw;
    public String[] args;
    public String name;
    public GUIPosition pos;
    public boolean commandTagUsed = false;

    public CommandTagEvent(DraimMenu plugin, GUI gui1, GUIPosition position, Player player, String rawCommand1) {
        this.p = player;
        this.gui =gui1;
        this.pos = position;

        boolean doApiPlaceholders = true;
        if(rawCommand1.startsWith("nopapi= ")){
            rawCommand1 = rawCommand1.replace("nopapi= ","");
            doApiPlaceholders = false;
        }

        String[] split = rawCommand1.split(" ", 2);
        if(split.length == 1){
            split = new String[]{split[0],""};
        }

        this.name = split[0].trim();
        this.raw = split[1].trim().split("\\s");
        if(doApiPlaceholders) {
            this.args = plugin.tex.attachPlaceholders(gui1,pos, player, split[1].trim()).split("\\s");
        }else{
            this.args = ChatColor.translateAlternateColorCodes('&',plugin.placeholders.setPlaceholders(gui, pos, p,split[1].trim(),false)).split("\\s");
            this.args = ChatColor.translateAlternateColorCodes('&',plugin.placeholders.setPlaceholders(gui, pos, p,split[1].trim(),true)).split("\\s");
        }
    }

    public void commandTagUsed(){
        commandTagUsed = true;
    }

    private static final HandlerList HANDLERS = new HandlerList();
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
