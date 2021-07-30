package me.draimgoose.draimmenu.commandtags.tags.economy;

import me.realized.tokenmanager.api.TokenManager;
import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.commandtags.CommandTagEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.Objects;

public class BuyCommandTags implements Listener {
    DraimMenu plugin;
    public BuyCommandTags(DraimMenu pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("buycommand=")){
            e.commandTagUsed();
            try {
                if (plugin.econ != null) {
                    if (plugin.econ.getBalance(e.p) >= Double.parseDouble(e.args[0])) {
                        plugin.econ.withdrawPlayer(e.p, Double.parseDouble(e.args[0]));
                        String price = e.args[0];
                        String command = String.join(" ",Arrays.copyOfRange(e.raw, 1, e.raw.length));
                        plugin.commandTags.runCommand(e.gui,e.pos,e.p,command);
                        plugin.tex.sendMessage(e.p,plugin.config.getString("purchase.currency.success").replaceAll("%dm-args%", price));
                    } else {
                        plugin.tex.sendMessage(e.p, plugin.config.getString("purchase.currency.failure"));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Для метода оплаты требуется Vault и Economy!");
                }
            } catch (Exception buyc) {
                plugin.debug(buyc,e.p);
                plugin.tex.sendMessage(e.p,plugin.config.getString("config.format.error") + " " + "команды: " + e.name);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("tokenbuycommand=")){
            e.commandTagUsed();
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    assert api != null;
                    int balance = Integer.parseInt(Long.toString(api.getTokens(e.p).orElse(0)));
                    if (balance >= Double.parseDouble(e.args[0])) {
                        api.removeTokens(e.p, Long.parseLong(e.args[0]));
                        //execute command under here
                        String price = e.args[0];
                        String command = String.join(" ",Arrays.copyOfRange(e.raw, 1, e.raw.length));
                        plugin.commandTags.runCommand(e.gui,e.pos,e.p,command);
                        plugin.tex.sendMessage(e.p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.success")).replaceAll("%dm-args%", price));
                    } else {
                        plugin.tex.sendMessage(e.p, plugin.config.getString("purchase.tokens.failure"));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Для метода оплаты требуется Vault и Economy!");
                }
            } catch (Exception buyc) {
                plugin.debug(buyc,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "команды: " + e.name);
            }
        }
    }
}
