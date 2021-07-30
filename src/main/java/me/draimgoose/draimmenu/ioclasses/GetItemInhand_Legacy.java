package me.draimgoose.draimmenu.ioclasses;

import me.draimgoose.draimmenu.DraimMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetItemInhand_Legacy {
    DraimMenu plugin;
    public GetItemInhand_Legacy(DraimMenu pl) {
        this.plugin = pl;
    }

    @SuppressWarnings("deprecation")
    public ItemStack itemInHand(Player p){
        return p.getInventory().getItemInHand();
    }
}
