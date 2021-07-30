package me.draimgoose.draimmenu.commands;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandGUIReload implements CommandExecutor {
    DraimMenu plugin;
    public CommandGUIReload(DraimMenu pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("dmr") || label.equalsIgnoreCase("draimmenureload") || label.equalsIgnoreCase("dmenur")) {
            if (sender.hasPermission("draimmenu.reload")) {
                for(String name : plugin.openGUIs.openGUIs.keySet()){
                    plugin.openGUIs.closeGUIForLoader(name, GUIPosition.Top);
                    try {
                        Bukkit.getPlayer(name).closeInventory();
                    }catch (Exception ignore){}
                }

                plugin.reloadGUIFiles();
                if(new File(plugin.getDataFolder() + File.separator + "temp.yml").delete()){
                }
                plugin.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "config.yml"));
                plugin.blockConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "blocks.yml"));

                plugin.checkDuplicateGUI(sender);

                plugin.hotbar.reloadHotbarSlots();

                if(plugin.config.getString("config.auto-register-commands").equalsIgnoreCase("true")) {
                    registerCommands();
                }

                plugin.tag = plugin.tex.colour(plugin.config.getString("config.format.tag") + " ");
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.reload")));
            }else{
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Используйте: /dmr"));
        return true;
    }

    public void registerCommands(){
        File commandsLoc = new File("commands.yml");
        YamlConfiguration cmdCF;
        try {
            cmdCF = YamlConfiguration.loadConfiguration(commandsLoc);
        }catch(Exception e){
            plugin.debug(e,null);
            return;
        }

        for(String existingCommands : cmdCF.getConfigurationSection("aliases").getKeys(false)){
            try {
                if (cmdCF.getStringList("aliases." + existingCommands).get(0).equals("draimmenu")) {
                    cmdCF.set("aliases." + existingCommands, null);
                }
            }catch(Exception ignore){}
        }

        ArrayList<String> temp = new ArrayList<>();
        temp.add("draimmenu");

        for (GUI gui : plugin.guiList) {
            if(gui.getConfig().contains("guiType")){
                if(gui.getConfig().getStringList("guiType").contains("nocommandregister")){
                    continue;
                }
            }

            if(gui.getConfig().contains("commands")){
                List<String> guiCommands = gui.getConfig().getStringList("commands");
                for(String command : guiCommands){
                    cmdCF.set("aliases." + command.split("\\s")[0],temp);
                }
            }
        }

        try {
            cmdCF.save(commandsLoc);
        } catch (IOException var10) {
            Bukkit.getConsoleSender().sendMessage("(DraimMenu)" + ChatColor.RED + " ПРЕДУПРЕЖДЕНИЕ: Не удалось зарегистрировать кастомные команды!");
        }
    }
}
