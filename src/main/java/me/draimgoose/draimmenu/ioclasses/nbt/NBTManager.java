package me.draimgoose.draimmenu.ioclasses.nbt;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.ioclasses.legacy.MinecraftVersions;
import org.bukkit.inventory.ItemStack;

public class NBTManager {
    DraimMenu plugin;
    public NBTManager(DraimMenu pl) {
        this.plugin = pl;
    }

    public boolean hasNBT(ItemStack item){
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_13)){
            return new NBT_1_13().contains(item, "DraimMenuItem");
        }else{
            return new NBT_1_14(plugin).hasNBT(item,"DraimMenuItem");
        }
    }

    public ItemStack setNBT(ItemStack item){
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_13)){
            return new NBT_1_13().set(item,1,"DraimMenuItem");
        }else{
            return new NBT_1_14(plugin).addNBT(item,"DraimMenuItem","1");
        }
    }

    public String getNBT(ItemStack item, String key){
        String output = "";
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_13)){
            try{
                output = new NBT_1_13().getString(item, key);
            }catch(NullPointerException ignore){}
        }else{
            output = new NBT_1_14(plugin).getNBT(item, key);
        }
        return output;
    }

    public ItemStack setNBT(ItemStack item, String key, String value){
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_13)){
            return new NBT_1_13().set(item,value,key);
        }else{
            return new NBT_1_14(plugin).addNBT(item,key,value);
        }
    }
}
