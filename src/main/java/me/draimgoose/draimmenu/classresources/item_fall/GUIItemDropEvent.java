package me.draimgoose.draimmenu.classresources.item_fall;

import me.draimgoose.draimmenu.api.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class GUIItemDropEvent extends Event implements Cancellable {

    private boolean isCancelled;
    private final Player p;
    private final GUI gui;
    private final ItemStack item;

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public GUIItemDropEvent(Player player, GUI gui, ItemStack drop) {
        this.p = player;
        this.gui = gui;
        this.item = drop;
    }

    public Player getPlayer(){
        return this.p;
    }

    public ItemStack getItem(){
        return this.item;
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
