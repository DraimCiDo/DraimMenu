package me.draimgoose.draimmenu.playerinventoryhandler;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUIOpenedEvent;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventorySaver implements Listener {
    DraimMenu plugin;
    public InventorySaver(DraimMenu pl) {
        this.plugin = pl;
    }

    public YamlConfiguration inventoryConfig;

    public void saveInventoryFile(){
        try {
            inventoryConfig.save(plugin.getDataFolder() + File.separator + "inventories.yml");
        } catch (IOException s) {
            s.printStackTrace();
            plugin.debug(s,null);
        }
    }

    @EventHandler
    public void onOpen(GUIOpenedEvent e){
        if(e.getPosition() != GUIPosition.Top) {
            addInventory(e.getPlayer());
        }
    }

    @EventHandler
    public void playerJoined(PlayerJoinEvent e){
        restoreInventory(e.getPlayer(), GUIPosition.Top);
    }

    public void restoreInventory(Player p, GUIPosition position){
        if(p == null){
            return;
        }
        if(plugin.openGUIs.hasGUIOpen(p.getName(), GUIPosition.Middle) || plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Bottom)){
            if(position == GUIPosition.Bottom){
                for(int s = 0; s < 9; s++){
                    p.getInventory().setItem(s,null);
                }
            }else if(position == GUIPosition.Middle){
                for(int s = 9; s < 36; s++){
                    p.getInventory().setItem(s,null);
                }
            }
            return;
        }
        if(inventoryConfig.isSet(p.getUniqueId().toString())){
            p.getInventory().setContents(plugin.itemSerializer.itemStackArrayFromBase64(inventoryConfig.getString(p.getUniqueId().toString())));
            inventoryConfig.set(p.getUniqueId().toString(),null);
        }
    }

    public void addInventory(Player p){
        if(!inventoryConfig.contains(p.getUniqueId().toString())){
            inventoryConfig.set(p.getUniqueId().toString(),plugin.itemSerializer.itemStackArrayToBase64(p.getInventory().getContents()));
            p.getInventory().clear();
        }
    }

    public ItemStack[] getNormalInventory(Player p){
        if(hasNormalInventory(p)){
            return p.getInventory().getContents();
        }else{
            return plugin.itemSerializer.itemStackArrayFromBase64(inventoryConfig.getString(p.getUniqueId().toString()));
        }
    }

    public boolean hasNormalInventory(Player p){
        return !inventoryConfig.isSet(p.getUniqueId().toString());
    }

    public void addItem(Player p, ItemStack item){
        if(hasNormalInventory(p)){
            if (p.getInventory().firstEmpty() >= 0) {
                p.getInventory().addItem(item);
                return;
            }
        }else {
            List<ItemStack> cont = new ArrayList<>(Arrays.asList(getNormalInventory(p)));
            boolean found = false;
            for (int i = 0; 36 > i; i++){
                if(cont.get(i) == null){
                    cont.set(i,item);
                    found = true;
                    break;
                }
                if(cont.get(i).isSimilar(item)){
                    cont.get(i).setAmount(cont.get(i).getAmount()+1);
                    found = true;
                    break;
                }
            }
            if(found){
                inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                return;
            }
        }
        p.getLocation().getWorld().dropItemNaturally(p.getLocation(), item);
    }
}

