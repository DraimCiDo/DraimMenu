package me.draimgoose.draimmenu.api;

import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

public class GUIClosedEvent extends Event{

    private final Player p;
    private final GUI gui;
    private final GUIPosition pos;

    public GUIClosedEvent(Player player, GUI gui, GUIPosition position) {
        this.p = player;
        this.gui = gui;
        this.pos = position;
    }

    public Player getPlayer(){
        return this.p;
    }

    public GUIPosition getPosition(){
        return this.pos;
    }

    public Inventory getInventory(){
        return this.p.getInventory();
    }

    public GUI getGui(){
        return this.gui;
    }

    private static final HandlerList HANDLERS = new HandlerList();
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
