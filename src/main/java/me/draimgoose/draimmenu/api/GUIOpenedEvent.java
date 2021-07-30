package me.draimgoose.draimmenu.api;

import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

public class GUIOpenedEvent extends Event implements Cancellable {

    private boolean isCancelled;
    private final Player p;
    private final GUI gui;
    private final GUIPosition pos;

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public GUIOpenedEvent(Player player, GUI gui, GUIPosition position) {
        this.p = player;
        this.gui = gui;
        this.pos = position;
    }

    public GUIPosition getPosition(){
        return this.pos;
    }

    public Player getPlayer(){
        return this.p;
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
