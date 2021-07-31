package me.draimgoose.draimmenu.generatepanels;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.ioclasses.legacy.MinecraftVersions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class GenUtils implements Listener {
    public YamlConfiguration tempEdit;
    DraimMenu plugin;
    public GenUtils(DraimMenu pl) {
        this.plugin = pl;
        this.tempEdit = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "temp.yml"));
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player)e.getPlayer();
        if(!ChatColor.stripColor(e.getView().getTitle()).equals("Создание нового меню")){
            return;
        }
        plugin.reloadGUIFiles();
        generateGUI(p,e.getInventory());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        this.plugin.generateMode.remove(p);
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e) {
        HumanEntity h = e.getPlayer();
        Player p = Bukkit.getPlayer(h.getName());
        if (this.plugin.generateMode.contains(p)) {
            this.plugin.generateMode.remove(p);
            generateGUI(p,e.getInventory());
        }
    }

    @SuppressWarnings("deprecation")
    void generateGUI(Player p, Inventory inv){
        ArrayList<String> agui = new ArrayList();
        for(GUI gui : plugin.guiList){
            agui.add(gui.getName());
        }
        boolean foundItem = false;
        for(ItemStack temp : inv.getContents()){
            if(temp != null){
                foundItem = true;
                break;
            }
        }
        if(!foundItem){
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Отмененное меню!"));
            return;
        }
        YamlConfiguration file;
        String date = "gui-1";
        for(int count = 1; (Arrays.asList(Objects.requireNonNull(plugin.guiSF.list())).contains("gui-" + count + ".yml")) || (agui.contains("gui-" + count)); count++){
            date = "gui-" + (count+1);
        }
        File folder = new File(plugin.getDataFolder() + File.separator + "gui");
        file = YamlConfiguration.loadConfiguration(new File(folder + File.separator + date + ".yml"));
        file.set("gui." + date + ".perm", "default");

        if(inv.getType().toString().contains("CHEST")){
            file.set("gui." + date + ".rows", inv.getSize()/9);
        }else{
            file.set("gui." + date + ".rows", inv.getType().toString());
        }

        file.set("gui." + date + ".title", "&8Сгенерированно " + date);
        file.addDefault("gui." + date + ".command", date);
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)) {
            file.set("gui." + date + ".empty", "STAINED_GLASS_PANE");
            file.set("gui." + date + ".emptyID", "15");
        }else{
            file.set("gui." + date + ".empty", "BLACK_STAINED_GLASS_PANE");
        }
        file = plugin.itemCreate.generateGUIFile(date,inv,file);

        try {
            file.save(new File(plugin.guiSF + File.separator + date + ".yml"));
            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Сохранение сгенерированного файла: " + date + ".yml"));
        } catch (IOException var16) {
            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не удалось сохранить созданную панель!"));
        }
        plugin.reloadGUIFiles();
    }
}
