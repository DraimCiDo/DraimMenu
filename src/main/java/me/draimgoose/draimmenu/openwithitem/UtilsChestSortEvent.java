package me.draimgoose.draimmenu.openwithitem;

import de.jeff_media.chestsort.api.ChestSortEvent;
import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;

public class UtilsChestSortEvent implements Listener {
    DraimMenu plugin;
    public UtilsChestSortEvent(DraimMenu pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onChestSortEvent(ChestSortEvent e){
        //если игрок равен нулю, это не обязательно
        if(e.getPlayer() == null){
            return;
        }
        //отмена, если гуи открыта у всех
        if(plugin.openGUIs.hasGUIOpen(e.getPlayer().getName(), GUIPosition.Top)){
            e.setCancelled(true);
            return;
        }
        //код хотбара
        if(!plugin.openWithItem){
            //если ни одна из гуищек не открыта у всех
            return;
        }
        //Если плагин ChestSort запускает событие
        if(e.getInventory().getType() == InventoryType.PLAYER){
            for(int slot : plugin.hotbar.stationaryItems.get(e.getPlayer().getUniqueId()).list.keySet()){
                e.setUnmovable(slot);
            }
        }
    }
}
