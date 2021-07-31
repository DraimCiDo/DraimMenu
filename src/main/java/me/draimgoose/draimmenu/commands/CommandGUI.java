package me.draimgoose.draimmenu.commands;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class CommandGUI implements CommandExecutor {
    DraimMenu plugin;
    public CommandGUI(DraimMenu pl) {
        this.plugin = pl;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        GUI gui = null;
        if (args.length != 0) {
            for(GUI tempGUI  : plugin.guiList){
                if(tempGUI.getName().equals(args[0])) {
                    gui = tempGUI;
                    break;
                }
            }
        }else{
            plugin.helpMessage(sender);
            return true;
        }
        if(gui == null){
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.nomenu")));
            return true;
        }
        boolean disableCommand = false;
        if(gui.getConfig().contains("guiType")) {
            if (gui.getConfig().getStringList("guiType").contains("nocommand")) {
                disableCommand =  true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("dm") || cmd.getName().equalsIgnoreCase("draimmenu") || cmd.getName().equalsIgnoreCase("dmenu")) {
            if(!(sender instanceof Player)) {
                if(args.length == 2){
                    if(!args[1].equals("item")){
                        if(!disableCommand) {
                            plugin.openVoids.openCommandGUI(sender, plugin.getServer().getPlayer(args[1]), gui.copy(), GUIPosition.Top, true);
                        }
                    }else{
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Используйте: /dm <gui> [item] [player]"));
                    }
                    return true;
                }else if(args.length == 3){
                    if (args[1].equals("item")) {
                        plugin.openVoids.giveHotbarItem(sender,plugin.getServer().getPlayer(args[2]),gui.copy(),true);
                    }else{
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Используйте: /dm <gui> item [player]"));
                    }
                    return true;
                } else {
                    sender.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Пожалуйста, выполните команду от игрока!"));
                    return true;
                }
            }else{
                Player p = (Player) sender;
                if (args.length == 1) {
                    if(!disableCommand) {
                        plugin.openVoids.openCommandGUI(sender, p, gui.copy(),GUIPosition.Top, false);
                    }
                    return true;
                }else if(args.length == 2){
                    if (args[1].equals("item")) {
                        plugin.openVoids.giveHotbarItem(sender, p, gui.copy(), false);
                    }else{
                        if(!disableCommand) {
                            plugin.openVoids.openCommandGUI(sender, plugin.getServer().getPlayer(args[1]), gui.copy(),GUIPosition.Top, true);
                        }
                    }
                    return true;
                }else if(args.length == 3){
                    plugin.openVoids.giveHotbarItem(sender, plugin.getServer().getPlayer(args[2]), gui.copy(),true);
                    return true;
                }
            }
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Используйте: /dm <gui> [player:item] [player]"));
        return true;
    }
}
