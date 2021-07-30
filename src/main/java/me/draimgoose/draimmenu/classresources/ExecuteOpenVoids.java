package me.draimgoose.draimmenu.classresources;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.api.GUIOpenedEvent;
import me.draimgoose.draimmenu.openguimanager.GUIOpenType;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.Objects;

public class ExecuteOpenVoids {
    DraimMenu plugin;
    public ExecuteOpenVoids(DraimMenu pl) {
        this.plugin = pl;
    }

    public void openCommandGUI(CommandSender sender, Player p, GUI gui, GUIPosition position, boolean openForOtherUser){
        if(p.isSleeping()){
            return;
        }
        if(plugin.debug.isEnabled(sender) || plugin.config.getBoolean("config.auto-update-gui")){
            gui.setConfig(YamlConfiguration.loadConfiguration(gui.getFile()));
        }
        if (!sender.hasPermission("draimmenu.gui." + gui.getConfig().getString("perm"))) {
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            return;
        }
        if(sender.hasPermission("draimmenu.other") || !openForOtherUser) {
            if(!plugin.guiPerms.isGUIWorldEnabled(p,gui.getConfig())){
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                return;
            }

            if(position != GUIPosition.Top && !plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Top)){
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Невозможно открыть панель без панели вверху."));
                return;
            }

            if(!plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Top) && p.getOpenInventory().getType() != InventoryType.CRAFTING){
                p.closeInventory();
            }

            GUIOpenedEvent openedEvent = new GUIOpenedEvent(p,gui,position);
            Bukkit.getPluginManager().callEvent(openedEvent);
            if(openedEvent.isCancelled()){
                return;
            }

            beforeLoadCommands(gui,position,p);

            try {
                plugin.createGUI.openGui(gui, p, position,GUIOpenType.Normal,0);

                if (gui.getConfig().contains("commands-on-open")) {
                    try {
                        plugin.commandTags.runCommands(gui,position,p, gui.getConfig().getStringList("commands-on-open"));
                    }catch(Exception s){
                        p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " " + "commands-on-open: " + gui.getConfig().getString("commands-on-open")));
                    }
                }

                if (gui.getConfig().contains("sound-on-open")) {
                    if(!Objects.requireNonNull(gui.getConfig().getString("sound-on-open")).equalsIgnoreCase("off")) {
                        try {
                            p.playSound(p.getLocation(), Sound.valueOf(Objects.requireNonNull(gui.getConfig().getString("sound-on-open")).toUpperCase()), 1F, 1F);
                        } catch (Exception s) {
                            p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " " + "sound-on-open: " + gui.getConfig().getString("sound-on-open")));
                        }
                    }
                }

                if(openForOtherUser) {
                    sender.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Панель открыта для " + p.getDisplayName()));
                }
            } catch (Exception r) {
                plugin.debug(r,null);
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error")));
                plugin.openGUIs.closeGUIForLoader(p.getName(),position);
                p.closeInventory();
            }
        }else{
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
        }
    }

    public void giveHotbarItem(CommandSender sender, Player p, GUI gui, boolean sendGiveMessage){
        if (sender.hasPermission("draimmenu.item." + gui.getConfig().getString("perm")) && gui.getConfig().contains("open-with-item")) {
            if(!plugin.guiPerms.isGUIWorldEnabled(p,gui.getConfig())){
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                return;
            }

            if(sender.hasPermission("draimmenu.other") || !sendGiveMessage) {
                try {
                    if(gui.getConfig().contains("open-with-item.stationary")) {
                        p.getInventory().setItem(Integer.parseInt(Objects.requireNonNull(gui.getConfig().getString("open-with-item.stationary"))), gui.getHotbarItem(p));
                    }else{
                        p.getInventory().addItem(gui.getHotbarItem(p));
                    }
                    if(sendGiveMessage) {
                        sender.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Предмет выдан игроку " + p.getDisplayName()));
                    }
                } catch (Exception r) {
                    sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.notitem")));
                }
            }else{
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return;
        }
        if (!gui.getConfig().contains("open-with-item")) {
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.noitem")));
            return;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
    }

    public void beforeLoadCommands(GUI gui,GUIPosition pos, Player p){
        if (gui.getConfig().contains("pre-load-commands")) {
            try {
                plugin.commandTags.runCommands(gui,pos,p, gui.getConfig().getStringList("pre-load-commands"));
            }catch(Exception s){
                plugin.debug(s,p);
            }
        }
    }
}
