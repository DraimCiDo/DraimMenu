package me.draimgoose.draimmenu.ioclasses;

import me.draimgoose.draimmenu.DraimMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetItemInhand {
    DraimMenu plugin;
    public GetItemInhand(DraimMenu pl) {
        this.plugin = pl;
    }

    public ItemStack itemInHand(Player p){
        return p.getInventory().getItemInMainHand();
    }
}
