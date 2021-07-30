package me.draimgoose.draimmenu.commandtags.tags.economy;

import me.realized.tokenmanager.api.TokenManager;
import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.commandtags.CommandTagEvent;
import me.draimgoose.draimmenu.ioclasses.legacy.MinecraftVersions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class BuyItemTags implements Listener {
    DraimMenu plugin;
    public BuyItemTags(DraimMenu pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("buy=")){
            e.commandTagUsed();
            try {
                if (plugin.econ != null) {
                    if (plugin.econ.getBalance(e.p) >= Double.parseDouble(e.args[0])) {
                        plugin.econ.withdrawPlayer(e.p, Double.parseDouble(e.args[0]));
                        plugin.tex.sendMessage(e.p, Objects.requireNonNull(plugin.config.getString("purchase.currency.success")).replaceAll("%dm-args%", e.args[0]));
                        giveItem(e.p, e.args);
                    } else {
                        plugin.tex.sendMessage(e.p, plugin.config.getString("purchase.currency.failure"));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Для метода оплаты требуется Vault и Economy!");
                }
            } catch (Exception buy) {
                plugin.debug(buy,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "команды: " + e.name);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("tokenbuy=")) {
            e.commandTagUsed();
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    assert api != null;
                    int balance = Integer.parseInt(Long.toString(api.getTokens(e.p).orElse(0)));
                    if (balance >= Double.parseDouble(e.args[0])) {
                        api.removeTokens(e.p, Long.parseLong(e.args[0]));
                        plugin.tex.sendMessage(e.p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.success")).replaceAll("%dm-args%", e.args[0]));
                        giveItem(e.p,e.args);
                    } else {
                        plugin.tex.sendMessage(e.p, plugin.config.getString("purchase.tokens.failure"));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Для метода оплаты требуется Vault и Economy!!");
                }
            } catch (Exception buy) {
                plugin.debug(buy, e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void giveItem(Player p, String[] args){
        byte id = 0;
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)) {
            for (String argsTemp : args) {
                if (argsTemp.startsWith("id:")) {
                    id = Byte.parseByte(argsTemp.replace("id:", ""));
                    break;
                }
            }
        }
        plugin.inventorySaver.addItem(p,new ItemStack(Objects.requireNonNull(Material.matchMaterial(args[1])), Integer.parseInt(args[2]),id));
    }
}

