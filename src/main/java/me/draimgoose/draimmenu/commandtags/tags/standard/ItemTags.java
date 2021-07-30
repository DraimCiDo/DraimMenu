package me.draimgoose.draimmenu.commandtags.tags.standard;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.commandtags.CommandTagEvent;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ItemTags implements Listener {
    DraimMenu plugin;
    public ItemTags(DraimMenu pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("give-item=")){
            e.commandTagUsed();
            ItemStack itm = plugin.itemCreate.makeCustomItemFromConfig(null,e.pos,e.gui.getConfig().getConfigurationSection("custom-item." + e.args[0]), e.p, true, true, false);
            plugin.inventorySaver.addItem(e.p,itm);
            return;
        }
        if(e.name.equalsIgnoreCase("setitem=")){
            e.commandTagUsed();
            ItemStack s = plugin.itemCreate.makeItemFromConfig(null, e.pos,e.gui.getConfig().getConfigurationSection("custom-item." + e.args[0]), e.p, true, true, true);
            GUIPosition position = GUIPosition.valueOf(e.args[2]);
            if(position == GUIPosition.Top) {
                e.p.getOpenInventory().getTopInventory().setItem(Integer.parseInt(e.args[1]), s);
            }else if(position == GUIPosition.Middle) {
                e.p.getInventory().setItem(Integer.parseInt(e.args[1])+9, s);
            }else{
                e.p.getInventory().setItem(Integer.parseInt(e.args[1]), s);
            }
        }
    }
}
