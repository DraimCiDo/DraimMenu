package me.draimgoose.draimmenu.classresources.item_fall;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUIClosedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ItemFallManager implements Listener {
    DraimMenu plugin;
    public ItemFallManager(DraimMenu pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void GUICloseItemsDrop(GUIClosedEvent e){
        new BukkitRunnable(){
            @Override
            public void run(){
                for(String item : e.getGui().getConfig().getConfigurationSection("item").getKeys(false)){
                    if(e.getGui().getConfig().isSet("item." + item + ".itemType")){
                        if(e.getGui().getConfig().getStringList("item." + item + ".itemType").contains("dropItem")){
                            ItemStack stack = e.getPlayer().getOpenInventory().getTopInventory().getItem(Integer.parseInt(item));
                            if(stack == null || stack.getType() == Material.AIR){
                                continue;
                            }

                            GUIItemDropEvent dropEvent = new GUIItemDropEvent(e.getPlayer(),e.getGui(),stack);
                            Bukkit.getPluginManager().callEvent(dropEvent);
                            if(dropEvent.isCancelled()){
                                continue;
                            }

                            e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(),stack);
                        }else if(e.getGui().getConfig().getStringList("item." + item + ".itemType").contains("returnItem")){
                            ItemStack stack = e.getPlayer().getOpenInventory().getTopInventory().getItem(Integer.parseInt(item));
                            if(stack == null || stack.getType() == Material.AIR){
                                continue;
                            }
                            plugin.inventorySaver.addItem(e.getPlayer(),stack);
                        }
                    }
                }
            }
        }.run();
    }
}
