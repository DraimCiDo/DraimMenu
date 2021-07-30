package me.draimgoose.draimmenu.ioclasses;

import me.draimgoose.draimmenu.DraimMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GetStorageContents {
    DraimMenu plugin;
    public GetStorageContents(DraimMenu pl) {
        this.plugin = pl;
    }

    public ItemStack[] getStorageContents(Inventory i){
        return i.getContents();
    }

    public void setStorageContents(Player p, ItemStack[] i){
        p.getOpenInventory().getTopInventory().setContents(i);
    }
}
