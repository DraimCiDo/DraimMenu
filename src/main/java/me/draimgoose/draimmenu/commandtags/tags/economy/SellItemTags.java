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
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SellItemTags implements Listener {
    DraimMenu plugin;
    public SellItemTags(DraimMenu pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("sell=")){
            e.commandTagUsed();
            try {
                if (plugin.econ != null) {
                    boolean sold = removeItem(e.p, e.args);
                    if (!sold) {
                        plugin.tex.sendMessage(e.p, plugin.config.getString("purchase.item.failure"));
                    } else {
                        plugin.econ.depositPlayer(e.p, Double.parseDouble(e.args[0]));
                        plugin.tex.sendMessage(e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%dm-args%", e.args[1]));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Для метода оплаты требуется Vault и Economy!");
                }
            } catch (Exception sell) {
                plugin.debug(sell,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "команды: " + e.name);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("tokensell=")) {
            e.commandTagUsed();
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    boolean sold = removeItem(e.p, e.args);
                    if (!sold) {
                        plugin.tex.sendMessage(e.p, plugin.config.getString("purchase.item.failure"));
                    } else {
                        assert api != null;
                        api.addTokens(e.p, Long.parseLong(e.args[0]));
                        plugin.tex.sendMessage(e.p, plugin.config.getString("purchase.item.success").replaceAll("%dm-args%", e.args[1]));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Для метода оплаты требуется Vault и Economy!");
                }
            } catch (Exception sell) {
                plugin.debug(sell,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "команды: " + e.name);
            }
        }
    }

    private boolean removeItem(Player p, String[] args){
        List<ItemStack> cont = new ArrayList<>(Arrays.asList(plugin.inventorySaver.getNormalInventory(p)));

        for (int f = 0; f < 36; f++) {
            ItemStack itm = cont.get(f);
            if (itm != null && itm.getType().equals(Material.matchMaterial(args[1]))) {
                String potion = "false";
                for(String argsTemp : args){
                    if(argsTemp.startsWith("potion:")){
                        potion = argsTemp.replace("potion:","");
                    }
                }
                byte id = -1;
                if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)) {
                    for (String argsTemp : args) {
                        if (argsTemp.startsWith("id:")) {
                            id = Byte.parseByte(argsTemp.replace("id:", ""));
                            break;
                        }
                    }
                }
                try {
                    if (!potion.equals("false")) {
                        PotionMeta potionMeta = (PotionMeta) itm.getItemMeta();
                        assert potionMeta != null;
                        if (!potionMeta.getBasePotionData().getType().name().equalsIgnoreCase(potion)) {
                            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Ваш предмет имеет неправильные эффекты зелья"));
                            return false;
                        }
                    }
                    if (id != -1) {
                        if (itm.getDurability() != id) {
                            continue;
                        }
                    }
                }catch(Exception exc){
                    plugin.debug(exc,p);
                }
                if (itm.getAmount() >= new ItemStack(Objects.requireNonNull(Material.matchMaterial(args[1])), Integer.parseInt(args[2])).getAmount()) {
                    int amt = itm.getAmount() - new ItemStack(Objects.requireNonNull(Material.matchMaterial(args[1])), Integer.parseInt(args[2])).getAmount();
                    itm.setAmount(amt);
                    if(plugin.inventorySaver.hasNormalInventory(p)){
                        p.getInventory().setItem(f, amt > 0 ? itm : null);
                        p.updateInventory();
                    }else{
                        cont.set(f,amt > 0 ? itm : null);
                        plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
