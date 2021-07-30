package me.draimgoose.draimmenu.openwithitem;

import me.draimgoose.draimmenu.DraimMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class SwapItemEvent implements Listener {
    DraimMenu plugin;
    public SwapItemEvent(DraimMenu pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent e){
        if(!plugin.openWithItem){
            //если ни одна из панелей не действительна
            return;
        }
        Player p = e.getPlayer();
        try {
            if (plugin.hotbar.itemCheckExecute(e.getOffHandItem(), p, false, true)) {
                e.setCancelled(true);
                p.updateInventory();
            }
        }catch(Exception ignore){}
    }
}
