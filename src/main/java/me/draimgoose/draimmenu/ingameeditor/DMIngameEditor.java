package me.draimgoose.draimmenu.ingameeditor;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.openguimanager.GUIOpenType;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Objects;

public class DMIngameEditor implements CommandExecutor {
    DraimMenu plugin;

    public DMIngameEditor(DraimMenu pl) {
        this.plugin = pl;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("draimmenu.edit")){
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            return true;
        }
        if(Objects.requireNonNull(plugin.config.getString("config.ingame-editor")).equalsIgnoreCase("false")){
            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Редактор отключен!"));
            return true;
        }
        if(!(sender instanceof Player)) {
            sender.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Пожалуйста, выполните команду как игрок!"));
            return true;
        }
        Player p = (Player)sender;
        if (args.length == 1) {
            for(GUI gui  : plugin.guiList){
                if(gui.getName().equals(args[0])) {
                    plugin.createGUI.openGui(gui.copy(), p, GUIPosition.Top, GUIOpenType.Editor,0);
                    return true;
                }
            }
        }
        if (args.length == 0) {
            plugin.editorGUI.openEditorGUI(p,0);
            return true;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Используйте: /dme <меню>"));
        return true;
    }
}
