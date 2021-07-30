package me.draimgoose.draimmenu.openwithitem;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.ioclasses.GetItemInhand;
import me.draimgoose.draimmenu.ioclasses.GetItemInhand_Legacy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class UtilsOpenWithItem implements Listener {
    DraimMenu plugin;
    public UtilsOpenWithItem(DraimMenu pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onAnyClick(InventoryClickEvent e) {
        //щелчок мыши в любом инвентаре
        if(!plugin.openWithItem){
            //если ни одна из панелей не имеет open-with-item
            return;
        }
        Player p = (Player)e.getWhoClicked();
        //нажатие на предмет, а затем прохождение по именам гуишек после того, как действие не будет пустым
        if(e.getAction() == InventoryAction.NOTHING){return;}
        if(e.getSlot() == -999){return;}
        if(e.getClickedInventory() == null) {
            //пропуск, если значение равно нулю, чтобы остановить ошибки
            return;
        }
        if(e.getClickedInventory().getType() == InventoryType.PLAYER && !e.isCancelled()) {
            if (plugin.hotbar.stationaryExecute(e.getSlot(), p,e.getClick(), true)) {
                e.setCancelled(true);
                p.updateInventory();
            }
        }
    }
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent e){
        //предмет нажал ПКМ (НЕ ЛКМ, потому что это вызывает проблемы при взаимодействии с вещами)
        if(!plugin.openWithItem){
            //если ни одна из панелей не имеет open-with-item
            return;
        }
        try {
            if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK || Objects.requireNonNull(e.getItem()).getType() == Material.AIR){
                return;
            }
        }catch(Exception b){
            return;
        }
        Player p = e.getPlayer();
        if(plugin.hotbar.itemCheckExecute(e.getItem(),p,true,false)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        plugin.hotbar.updateHotbarItems(e.getPlayer());
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        plugin.hotbar.updateHotbarItems(e.getPlayer());
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        if(!plugin.openWithItem){
            //если ни одна из панелей не имеет open-with-item
            return;
        }
        Player p = e.getEntity();
        for(GUI gui : plugin.guiList) { //будет перебирать все файлы в папке
            if (p.hasPermission("draimmenu.gui." + gui.getConfig().getString("perm")) && gui.hasHotbarItem()) {
                if(gui.getConfig().contains("open-with-item.stationary")){
                    ItemStack s = gui.getHotbarItem(p);
                    e.getDrops().remove(s);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        plugin.hotbar.updateHotbarItems(e.getPlayer());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        plugin.hotbar.stationaryItems.remove(e.getPlayer().getUniqueId());
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e){
        if(!plugin.openWithItem){
            //если ни одна из панелей не имеет open-with-item
            return;
        }
        //если итем дропнули
        Player p = e.getPlayer();
        if(plugin.hotbar.itemCheckExecute(e.getItemDrop().getItemStack(),p,false,true)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e){
        if(!plugin.openWithItem){
            //если ни одна из панелей не имеет open-with-item
            return;
        }
        //отмена всего, если удерживают предмет (например, крутят рамку)
        Player p = e.getPlayer();
        ItemStack clicked;
        if(Bukkit.getVersion().contains("1.8")){
            clicked =  new GetItemInHand_Legacy(plugin).itemInHand(p);
        }else{
            clicked = new GetItemInHand(plugin).itemInHand(p);
        }
        if(plugin.hotbar.itemCheckExecute(clicked,p,true,false)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
}
