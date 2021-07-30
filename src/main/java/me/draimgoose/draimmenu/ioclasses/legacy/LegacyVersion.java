package me.draimgoose.draimmenu.ioclasses.legacy;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.ioclasses.GetStorageContents;
import me.draimgoose.draimmenu.ioclasses.GetStorageContents_Legacy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LegacyVersion {
    DraimMenu plugin;
    public MinecraftVersions LOCAL_VERSION;
    public LegacyVersion(DraimMenu pl) {
        this.plugin = pl;
        String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        LOCAL_VERSION = MinecraftVersions.get(VERSION);
    }

    public ItemStack[] getStorageContents(Inventory i){
        if(LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
            return new GetStorageContents_Legacy(plugin).getStorageContents(i);
        }else{
            return new GetStorageContents(plugin).getStorageContents(i);
        }
    }

    public void setStorageContents(Player p, ItemStack[] i){
        if(LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)){
            new GetStorageContents_Legacy(plugin).setStorageContents(p,i);
        }else{
            new GetStorageContents(plugin).setStorageContents(p,i);
        }
    }
}
