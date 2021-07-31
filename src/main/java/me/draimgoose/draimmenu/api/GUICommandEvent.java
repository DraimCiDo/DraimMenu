package me.draimgoose.draimmenu.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GUICommandEvent extends Event {

    private final Player p;
    private final String args;
    private final GUI gui;

    public GUICommandEvent(Player player, String message, GUI gui1) {
        this.p = player;
        this.args = message;
        this.gui = gui1;
    }

    public Player getPlayer(){
        return this.p;
    }

    public GUI getGUI(){
        return this.gui;
    }

    public String getMessage(){
        return this.args;
    }

    private static final HandlerList HANDLERS = new HandlerList();
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
