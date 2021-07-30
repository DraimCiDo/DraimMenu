package me.draimgoose.draimmenu.api;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.classresources.placeholders.GUIPlaceholders;
import me.draimgoose.draimmenu.openguimanager.GUIOpenType;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class GUI{
    DraimMenu plugin = JavaPlugin.getPlugin(DraimMenu.class);

    private ConfigurationSection guiConfig;
    private String guiName;
    private File guiFile;
    public GUIPlaceholders placeholders = new GUIPlaceholders();
    public boolean isOpen = false;

    public GUI(File file, String name){
        this.guiName = name;
        this.guiFile = file;
        this.guiConfig = YamlConfiguration.loadConfiguration(file).getConfigurationSection("gui." + name);
    }
    public GUI(ConfigurationSection config, String name){
        if(config.contains("gui")){
            config = config.getConfigurationSection("gui." + name);
        }
        this.guiName = name;
        this.guiConfig = config;
    }
    public GUI(String name){
        this.guiName = name;
    }

    public void setName(String name){
        this.guiName = name;
    }

    public void setConfig(ConfigurationSection config){
        if(config.contains("gui")){
            config = config.getConfigurationSection("gui." + this.guiName);
        }
        this.guiConfig = config;
    }

    public void setFile(File file){
        this.guiFile = file;
        this.guiConfig = YamlConfiguration.loadConfiguration(file).getConfigurationSection("gui." + this.getName());
    }

    public String getName(){
        return this.guiName;
    }

    public ConfigurationSection getConfig(){
        return this.guiConfig;
    }

    public File getFile(){
        return this.guiFile;
    }

    public ItemStack getItem(Player p, int slot){
        String section = plugin.itemCreate.hasSection(this,GUIPosition.Top,guiConfig.getConfigurationSection("item." + slot), p);
        ConfigurationSection itemSection = guiConfig.getConfigurationSection("item." + slot + section);
        return plugin.itemCreate.makeItemFromConfig(this,GUIPosition.Top,itemSection, p, true, true, false);
    }

    public ItemStack getCustomItem(Player p, String itemName){
        String section = plugin.itemCreate.hasSection(this,GUIPosition.Top,guiConfig.getConfigurationSection("custom-item." + itemName), p);
        ConfigurationSection itemSection = guiConfig.getConfigurationSection("custom-item." + itemName + section);
        return plugin.itemCreate.makeCustomItemFromConfig(this,GUIPosition.Top,itemSection, p, true, true, false);
    }

    public ItemStack getHotbarItem(Player p){
        ItemStack s = plugin.itemCreate.makeItemFromConfig(this,GUIPosition.Top,getHotbarSection(p), p, true, true, false);
        int slot = -1;
        if(getHotbarSection(p).isSet("stationary")){
            slot = getHotbarSection(p).getInt("stationary");
        }
        return plugin.nbt.setNBT(s,"DraimMenuHotbar",guiName + ":" + slot);
    }
    public ConfigurationSection getHotbarSection(Player p){
        String section = plugin.itemCreate.hasSection(this,GUIPosition.Top,guiConfig.getConfigurationSection("open-with-item"), p);
        return guiConfig.getConfigurationSection("open-with-item" + section);
    }

    public boolean hasHotbarItem(){
        return this.guiConfig.contains("open-with-item");
    }

    public Inventory getInventory(Player p){
        return plugin.createGUI.openGui(this,p,GUIPosition.Top, GUIOpenType.Return,0);
    }

    public void open(Player p, GUIPosition position){
        isOpen = true;
        plugin.openVoids.openCommandGUI(p, p, this, position, false);
    }

    public GUI copy(){
        if(guiFile != null){
            return new GUI(guiFile, guiName);
        }
        return new GUI(guiConfig, guiName);
    }
}
