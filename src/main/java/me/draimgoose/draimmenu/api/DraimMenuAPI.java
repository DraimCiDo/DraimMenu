package me.draimgoose.draimmenu.api;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DraimMenuAPI {
    DraimMenu plugin;
    public DraimMenuAPI(DraimMenu pl) {
        this.plugin = pl;
    }

    public boolean isGUIOpen(Player p){
        return plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Top);
    }

    public GUI getOpenGUI(Player p, GUIPosition position){
        return plugin.openGUIs.getOpenGUI(p.getName(), position);
    }

    public List<GUI> getGUILoaded(){
        return plugin.guiList;
    }

    public void addGUI(GUI gui) throws IOException{
        File addedFile = new File(plugin.guiSF + File.separator + gui.getName() + ".yml");
        YamlConfiguration newYaml = new YamlConfiguration();
        if(gui.getConfig().contains("gui")){
            newYaml.set("",gui.getConfig());
        }else{
            newYaml.set("gui." + gui.getName(),gui.getConfig());
        }
        newYaml.save(addedFile);
        plugin.reloadGUIFiles();
    }

    public void removeGUI(GUI gui){
        for(GUI guis : plugin.guiList){
            if(guis.getName().equals(gui.getName())){
                if(guis.getFile().delete()){
                    plugin.reloadGUIFiles();
                }
            }
        }
    }

    public GUI getGUI(String guiName){
        for(GUI gui : plugin.guiList) {
            if(gui.getName().equals(guiName)) {
                return gui;
            }
        }
        return null;
    }

    public boolean hasNormalInventory(Player p){
        return plugin.inventorySaver.hasNormalInventory(p);
    }

    public ItemStack makeItem(Player p, ConfigurationSection itemSection){
        return plugin.itemCreate.makeCustomItemFromConfig(null,GUIPosition.Top,itemSection, p, true, true, false);
    }
}
