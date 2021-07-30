package me.draimgoose.draimmenu.commandtags;

import me.realized.tokenmanager.api.TokenManager;
import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.commandtags.tags.economy.BuyCommandTags;
import me.draimgoose.draimmenu.commandtags.tags.economy.BuyItemTags;
import me.draimgoose.draimmenu.commandtags.tags.economy.SellItemTags;
import me.draimgoose.draimmenu.commandtags.tags.other.DataTags;
import me.draimgoose.draimmenu.commandtags.tags.other.PlaceholderTags;
import me.draimgoose.draimmenu.commandtags.tags.other.SpecialTags;
import me.draimgoose.draimmenu.commandtags.tags.standard.BasicTags;
import me.draimgoose.draimmenu.commandtags.tags.standard.BungeeTags;
import me.draimgoose.draimmenu.commandtags.tags.standard.ItemTags;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommandTags {
    DraimMenu plugin;
    public CommandTags(DraimMenu pl) {
        this.plugin = pl;
    }

    public void runCommands(GUI gui, GUIPosition position,Player p, List<String> commands, ClickType click){
        for (String command : commands) {
            command = plugin.commandTags.hasCorrectClick(command,click);
            if(command.equals("")){
                continue;
            }

            PaywallOutput val = plugin.commandTags.commandPayWall(gui,p,command);
            if(val == PaywallOutput.Blocked){
                break;
            }
            if(val == PaywallOutput.NotApplicable){
                plugin.commandTags.runCommand(gui,position,p, command);
            }
        }
    }

    public void runCommands(GUI gui, GUIPosition position,Player p, List<String> commands){
        for (String command : commands) {
            PaywallOutput val = plugin.commandTags.commandPayWall(gui,p,command);
            if(val == PaywallOutput.Blocked){
                break;
            }
            if(val == PaywallOutput.NotApplicable){
                plugin.commandTags.runCommand(gui,position,p, command);
            }
        }
    }

    public void runCommand(GUI gui, GUIPosition position,Player p, String commandRAW){
        CommandTagEvent tags = new CommandTagEvent(plugin,gui,position,p,commandRAW);
        Bukkit.getPluginManager().callEvent(tags);
        if(!tags.commandTagUsed){
            Bukkit.dispatchCommand(p, plugin.tex.placeholders(gui,position,p,commandRAW.trim()));
        }
    }

    public void registerBuiltInTags(){
        plugin.getServer().getPluginManager().registerEvents(new BuyCommandTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BuyItemTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SellItemTags(plugin), plugin);

        plugin.getServer().getPluginManager().registerEvents(new DataTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlaceholderTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SpecialTags(plugin), plugin);

        plugin.getServer().getPluginManager().registerEvents(new BasicTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BungeeTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemTags(plugin), plugin);
    }

    public String hasCorrectClick(String command, ClickType click){
        try {
            switch(command.split("\\s")[0]){
                case "right=":{
                    command = command.replace("right= ", "");
                    if (click != ClickType.RIGHT) {
                        return "";
                    }
                    break;
                }
                case "rightshift=":{
                    command = command.replace("rightshift= ", "");
                    if (click != ClickType.SHIFT_RIGHT) {
                        return "";
                    }
                    break;
                }
                case "left=":{
                    command = command.replace("left= ", "");
                    if (click != ClickType.LEFT) {
                        return "";
                    }
                    break;
                }
                case "leftshift=":{
                    command = command.replace("leftshift= ", "");
                    if (click != ClickType.SHIFT_LEFT) {
                        return "";
                    }
                    break;
                }
                case "middle=":{
                    command = command.replace("middle= ", "");
                    if (click != ClickType.MIDDLE) {
                        return "";
                    }
                    break;
                }
            }
            return command;
        } catch (Exception ex) {
            return "";
        }
    }

    @SuppressWarnings("deprecation")
    public PaywallOutput commandPayWall(GUI gui, Player p, String command) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        switch(command.split("\\s")[0]){
            case "paywall=": {
                try {
                    if (plugin.econ != null) {
                        if (plugin.econ.getBalance(p) >= Double.parseDouble(command.split("\\s")[1])) {
                            plugin.econ.withdrawPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                            plugin.tex.sendString(p,Objects.requireNonNull(plugin.config.getString("purchase.currency.success")).replaceAll("%dm-args%", command.split("\\s")[1]));
                            return PaywallOutput.Passed;
                        } else {
                            plugin.tex.sendString(p,plugin.config.getString("purchase.currency.failure"));
                            return PaywallOutput.Blocked;
                        }
                    } else {
                        plugin.tex.sendString(p, tag + ChatColor.RED + "Для метода оплаты требуется Vault и Economy!");
                        return PaywallOutput.Blocked;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "команды: " + command);
                    return PaywallOutput.Blocked;
                }
            }
            case "tokenpaywall=": {
                try {
                    if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                        TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                        assert api != null;
                        int balance = Integer.parseInt(Long.toString(api.getTokens(p).orElse(0)));
                        if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                            api.removeTokens(p, Long.parseLong(command.split("\\s")[1]));
                            plugin.tex.sendString(p,Objects.requireNonNull(plugin.config.getString("purchase.tokens.success")).replaceAll("%dm-args%", command.split("\\s")[1]));
                            return PaywallOutput.Passed;
                        } else {
                            plugin.tex.sendString(p,plugin.config.getString("purchase.tokens.failure"));
                            return PaywallOutput.Blocked;
                        }
                    } else {
                        plugin.tex.sendString(p, tag + ChatColor.RED + "Чтобы это работало, требуется TokenManager!");
                        return PaywallOutput.Blocked;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "команды: " + command);
                    return PaywallOutput.Blocked;
                }
            }
            case "item-paywall=": {
                List<ItemStack> cont = new ArrayList<>(Arrays.asList(plugin.inventorySaver.getNormalInventory(p)));
                try {
                    short id = 0;
                    if(command.split("\\s").length == 4){
                        id = Short.parseShort(command.split("\\s")[3]);
                    }

                    ItemStack sellItem;
                    if(command.split("\\s").length == 2) {
                        sellItem = plugin.itemCreate.makeCustomItemFromConfig(gui,GUIPosition.Top,gui.getConfig().getConfigurationSection("custom-item." + command.split("\\s")[1]), p, true, true, false);
                    }else{
                        sellItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[1])), Integer.parseInt(command.split("\\s")[2]), id);
                    }
                    PaywallOutput removedItem = PaywallOutput.Blocked;

                    for(int f = 0; f < 36; f++){

                        if(cont.get(f) == null){
                            continue;
                        }

                        if(command.split("\\s").length == 2){
                            if(plugin.itemCreate.isIdentical(sellItem,cont.get(f))){
                                if (sellItem.getAmount() <= cont.get(f).getAmount()) {
                                    if (plugin.inventorySaver.hasNormalInventory(p)) {
                                        p.getInventory().getItem(f).setAmount(cont.get(f).getAmount() - sellItem.getAmount());
                                        p.updateInventory();
                                    } else {
                                        cont.get(f).setAmount(cont.get(f).getAmount() - sellItem.getAmount());
                                        plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                                    }
                                    removedItem = PaywallOutput.Passed;
                                    break;
                                }
                            }
                        }else {
                            if (cont.get(f).getType() == sellItem.getType()) {
                                if (sellItem.getAmount() <= cont.get(f).getAmount()) {
                                    if(plugin.inventorySaver.hasNormalInventory(p)){
                                        p.getInventory().getItem(f).setAmount(cont.get(f).getAmount() - sellItem.getAmount());
                                        p.updateInventory();
                                    }else{
                                        cont.get(f).setAmount(cont.get(f).getAmount() - sellItem.getAmount());
                                        plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                                    }
                                    removedItem = PaywallOutput.Passed;
                                    break;
                                }
                            }
                        }
                    }

                    if(removedItem == PaywallOutput.Blocked){
                        plugin.tex.sendString(p, tag + plugin.config.getString("purchase.item.failure"));
                    }else{
                        plugin.tex.sendString(p,Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%dm-args%",sellItem.getType().toString()));
                    }
                    return removedItem;
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "команды: " + command);
                    return PaywallOutput.Blocked;
                }
            }
            case "xp-paywall=": {
                try {
                    int balance = p.getLevel();
                    if (balance >= Integer.parseInt(command.split("\\s")[1])) {
                        p.setLevel(p.getLevel() - Integer.parseInt(command.split("\\s")[1]));
                        plugin.tex.sendString(p,Objects.requireNonNull(plugin.config.getString("purchase.xp.success")).replaceAll("%dm-args%", command.split("\\s")[1]));
                        return PaywallOutput.Passed;
                    } else {
                        plugin.tex.sendString(p, plugin.config.getString("purchase.xp.failure"));
                        return PaywallOutput.Blocked;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "команды: " + command);
                    return PaywallOutput.Blocked;
                }
            }
        }
        return PaywallOutput.NotApplicable;
    }
}

