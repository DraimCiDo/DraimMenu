package me.draimgoose.draimmenu.commandtags.tags.standard;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUICommandEvent;
import me.draimgoose.draimmenu.commandtags.CommandTagEvent;
import me.draimgoose.draimmenu.openguimanager.GUIOpenType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BasicTags implements Listener {
    DraimMenu plugin;
    public BasicTags(DraimMenu pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("dmc")){
            e.commandTagUsed();
            e.p.closeInventory();
            return;
        }
        if(e.name.equalsIgnoreCase("refresh")) {
            e.commandTagUsed();
            if(plugin.openGUIs.hasGUIOpen(e.p.getName(),e.pos)) {
                plugin.createGUI.openGui(e.gui, e.p, e.pos, GUIOpenType.Refresh, 0);
            }
            if(plugin.inventorySaver.hasNormalInventory(e.p)){
                plugin.hotbar.updateHotbarItems(e.p);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("console=")) {
            e.commandTagUsed();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.join(" ",e.args));
            return;
        }
        if(e.name.equalsIgnoreCase("send=")) {
            e.commandTagUsed();
            e.p.chat(String.join(" ",e.args));
            return;
        }
        if(e.name.equalsIgnoreCase("sudo=")) {
            e.commandTagUsed();
            e.p.chat("/" + String.join(" ",e.args));
            return;
        }
        if(e.name.equalsIgnoreCase("msg=")) {
            e.commandTagUsed();
            plugin.tex.sendString(e.gui,e.pos,e.p,String.join(" ",e.args));
            return;
        }
        if(e.name.equalsIgnoreCase("op=")) {
            e.commandTagUsed();
            boolean isop = e.p.isOp();
            try {
                e.p.setOp(true);
                Bukkit.dispatchCommand(e.p,String.join(" ",e.args));
                e.p.setOp(isop);
            } catch (Exception exc) {
                e.p.setOp(isop);
                plugin.debug(exc,e.p);
                e.p.sendMessage(plugin.tag + plugin.tex.colour( plugin.config.getString("config.format.error") + " op=: Ошибка в команде op!"));
            }
            return;
        }
        if(e.name.equalsIgnoreCase("sound=")) {
            e.commandTagUsed();
            try {
                e.p.playSound(e.p.getLocation(), Sound.valueOf(e.args[0]), 1F, 1F);
            } catch (Exception s) {
                plugin.debug(s, e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "команды: " + e.args[0]);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("stopsound=")) {
            e.commandTagUsed();
            try {
                e.p.stopSound(Sound.valueOf(e.args[0]));
            } catch (Exception ss) {
                plugin.debug(ss, e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "команды: " + e.args[0]);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("event=")) {
            e.commandTagUsed();
            GUICommandEvent commandEvent = new GUICommandEvent(e.p, e.args[0], e.gui);
            Bukkit.getPluginManager().callEvent(commandEvent);
        }
    }
}
