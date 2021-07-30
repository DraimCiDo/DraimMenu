package me.draimgoose.draimmenu.openguimanager;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.api.GUIClosedEvent;
import me.draimgoose.draimmenu.ioclasses.nbt.NBT_1_13;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class UtilsGUILoader implements Listener {
    DraimMenu plugin;
    public UtilsGUILoader(DraimMenu pl) {
        this.plugin = pl;
    }

    //сообщяет загрузчику gui, что игрок открыл гуишку
    @EventHandler
    public void onPlayerCloseGUI(PlayerQuitEvent e){
        plugin.openGUIs.closeGUIForLoader(e.getPlayer().getName(),GUIPosition.Top);
        Player p = e.getPlayer();
        p.updateInventory();
        for(ItemStack itm : p.getInventory().getContents()){
            if(itm != null){
                if (plugin.nbt.hasNBT(itm)) {
                    p.getInventory().remove(itm);
                }
            }
        }
    }

    //сообщяет загрузчику gui, что игрок закрыл гуишку (в редакторах также есть один из них).
    @EventHandler
    public void onPlayerCloseGUI(InventoryCloseEvent e){
        //делает это только в том случае, если редактор отключен, так как он отключит этот код
        if(!Objects.requireNonNull(plugin.config.getString("config.ingame-editor")).equalsIgnoreCase("true")) {
            //это помещено здесь, чтобы избежать конфликтов, закрывает gui, если она закрыта
            plugin.openGUIs.closeGUIForLoader(e.getPlayer().getName(),GUIPosition.Top);
        }
    }

    @EventHandler
    public void onInventoryItemClick(InventoryClickEvent e){
        //это позволит убедиться, что предмет не из DraimMenu
        Player p = (Player)e.getWhoClicked();
        if(!plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Top)){
            for(ItemStack itm : p.getInventory().getContents()){
                if(plugin.openGUIs.isNBTInjected(itm)){
                    p.getInventory().remove(itm);
                }
            }
        }
    }

    //если обычное событие InventoryOpenEvent вызывается
    @EventHandler(priority = EventPriority.HIGHEST)
    public void vanillaOpenedEvent(InventoryOpenEvent e){
        if(e.isCancelled()) {
            if (plugin.openGUIs.hasGUIOpen(e.getPlayer().getName(),GUIPosition.Top)) {
                GUI closedGUI = plugin.openGUIs.getOpenGUI(e.getPlayer().getName(),GUIPosition.Top);

                //вручную удаляет игрока без проверок пропуска
                plugin.openGUIs.removePlayer(e.getPlayer().getName());

                //событие с закрытыми gui
                GUIClosedEvent closedEvent = new GUIClosedEvent(Bukkit.getPlayer(e.getPlayer().getName()),closedGUI, GUIPosition.Top);
                Bukkit.getPluginManager().callEvent(closedEvent);

                //сообщение
                if (plugin.config.contains("config.gui-snooper")) {
                    if (Objects.requireNonNull(plugin.config.getString("config.gui-snooper")).equalsIgnoreCase("true")) {
                        Bukkit.getConsoleSender().sendMessage("(DraimMenu) " + e.getPlayer().getName() + " GUI-панель была принудительно закрыта");
                    }
                }
            }
        }
    }
}
