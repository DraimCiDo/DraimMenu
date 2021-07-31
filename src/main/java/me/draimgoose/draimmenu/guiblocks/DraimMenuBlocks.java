package me.draimgoose.draimmenu.guiblocks;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class DraimMenuBlocks implements CommandExecutor {
    DraimMenu plugin;
    public DraimMenuBlocks(DraimMenu pl) { this.plugin = pl; }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("dmb") || label.equalsIgnoreCase("draimmenublock") || label.equalsIgnoreCase("dmenub")) {
            if(args.length >= 2) {
                if (args[0].equalsIgnoreCase("add")) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Пожалуйста, выполните команду как игрок!"));
                        return true;
                    }
                    Player p = (Player)sender;
                    if(p.hasPermission("draimmenu.block.add")){
                        if(Objects.requireNonNull(plugin.config.getString("config.gui-blocks")).equalsIgnoreCase("false")){
                            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Блоки отключены в конфигурации!"));
                            return true;
                        }
                        boolean foundGUI = false;
                        for(GUI temp : plugin.guiList){
                            if(temp.getName().equals(args[1])){
                                foundGUI = true;
                                break;
                            }
                        }
                        if(!foundGUI){
                            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.nogui")));
                            return true;
                        }
                        Block blockType = p.getTargetBlock(null, 5);
                        if(blockType.getType() == Material.AIR){
                            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Посмотрите на блок, чтобы добавить на него меню!"));
                            return true;
                        }
                        Location blockLocation = blockType.getLocation();
                        String configValue = "blocks." + Objects.requireNonNull(blockLocation.getWorld()).getName().replaceAll("_", "%dash%") + "_" + blockLocation.getBlockX() + "_" + blockLocation.getBlockY() + "_" + blockLocation.getBlockZ() + ".gui";
                        String guiValue = String.join(" ", args).replace("add ", "");
                        plugin.blockConfig.set(configValue, guiValue);
                        try {
                            plugin.blockConfig.save(new File(plugin.getDataFolder() + File.separator + "blocks.yml"));
                        } catch (IOException e) {
                            plugin.debug(e,p);
                            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Не удалось сохранить в файл!"));
                            return true;
                        }
                        String materialNameFormatted = blockType.getType().toString().substring(0, 1).toUpperCase() + blockType.getType().toString().substring(1).toLowerCase();
                        materialNameFormatted = materialNameFormatted.replaceAll("_"," ");
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + args[1] + ChatColor.GREEN + " теперь откроется при щелчке пкм " + ChatColor.WHITE + materialNameFormatted));
                    }else{
                        sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                    }
                    return true;
                }
            }
            if(args.length == 1){
                if (args[0].equalsIgnoreCase("remove")) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Пожалуйста, выполните команду как игрок!"));
                        return true;
                    }
                    Player p = (Player)sender;
                    if(p.hasPermission("draimmenu.block.remove")){
                        if(Objects.requireNonNull(plugin.config.getString("config.gui-blocks")).equalsIgnoreCase("false")){
                            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Блоки отключены в конфигурации!"));
                            return true;
                        }
                        Block blockType = p.getTargetBlock(null, 5);
                        Location blockLocation = blockType.getLocation();
                        String configValue = "blocks." + Objects.requireNonNull(blockLocation.getWorld()).getName().replaceAll("_", "%dash%") + "_" + blockLocation.getBlockX() + "_" + blockLocation.getBlockY() + "_" + blockLocation.getBlockZ() + ".gui";
                        if(plugin.blockConfig.contains(configValue)){
                            plugin.blockConfig.set(configValue.replace(".gui",""), null);
                            try {
                                plugin.blockConfig.save(new File(plugin.getDataFolder() + File.separator + "blocks.yml"));
                            } catch (IOException e) {
                                plugin.debug(e,p);
                                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Не удалось сохранить в файл!"));
                                return true;
                            }
                            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Меню было удалено из блока."));
                        }else{
                            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.nogui")));
                        }
                    }else{
                        sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("list")) {
                    if(sender.hasPermission("draimmenu.block.list")){
                        if(Objects.requireNonNull(plugin.config.getString("config.gui-blocks")).equalsIgnoreCase("false")){
                            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Блоки отключены в конфигурации!"));
                            return true;
                        }
                        if(plugin.blockConfig.contains("blocks")){
                            if(Objects.requireNonNull(plugin.blockConfig.getConfigurationSection("blocks")).getKeys(false).size() == 0){
                                sender.sendMessage(plugin.tex.colour(plugin.tag) + ChatColor.RED + "Привязанных блоков не найдено.");
                                return true;
                            }
                            sender.sendMessage(plugin.tex.colour(plugin.tag) + ChatColor.DARK_AQUA + "Расположение блоков с меню:");
                            for (String location : Objects.requireNonNull(plugin.blockConfig.getConfigurationSection("blocks")).getKeys(false)) {
                                sender.sendMessage(ChatColor.GREEN + location.replaceAll("_"," ") + ": " + ChatColor.WHITE + plugin.blockConfig.getString("blocks." + location + ".gui"));
                            }
                        }else{
                            sender.sendMessage(plugin.tex.colour(plugin.tag) + ChatColor.RED + "Привязанных блоков не найдено.");
                        }
                    }else{
                        sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                    }
                    return true;
                }
            }
        }
        plugin.helpMessage(sender);
        return true;
    }
}
