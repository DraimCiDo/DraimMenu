package me.draimgoose.draimmenu.classresources.placeholders;

import me.realized.tokenmanager.api.TokenManager;
import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.ioclasses.legacy.MinecraftVersions;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Placeholders {
    DraimMenu plugin;
    public Placeholders(DraimMenu pl) {
        this.plugin = pl;
    }

    public String setPlaceholders(GUI gui,GUIPosition position, Player p, String str, boolean primary){
        String[] HOLDERS = getPlaceholderEnds(gui,primary);
        while (str.contains(HOLDERS[0] + "dm-")) {
            try {
                int start = str.indexOf(HOLDERS[0] + "dm-");
                int end = str.indexOf(HOLDERS[1], str.indexOf(HOLDERS[0] + "dm-") + 1);
                String identifier = str.substring(start, end).replace(HOLDERS[0] + "dm-", "").replace(HOLDERS[1], "");
                String value;
                try {
                    value = dmPlaceholders(gui,position,p,identifier);
                } catch (NullPointerException er) {
                    value = "";
                }
                str = str.replace(str.substring(start, end) + HOLDERS[1], value);
            }catch(Exception ex){
                plugin.debug(ex,p);
                break;
            }
        }
        return str;
    }

    public String[] getPlaceholderEnds(GUI gui, boolean primary){
        List<String[]> values = new ArrayList<>();
        values.add(new String[]{plugin.config.getString("placeholders.primary.start"),plugin.config.getString("placeholders.primary.end")});
        values.add(new String[]{plugin.config.getString("placeholders.secondary.start"),plugin.config.getString("placeholders.secondary.end")});
        if(gui != null) {
            if (gui.getConfig().isSet("placeholders")) {
                if (gui.getConfig().isSet("placeholders.primary")) {
                    values.set(0, new String[]{gui.getConfig().getString("placeholders.primary.start"), gui.getConfig().getString("placeholders.primary.end")});
                }
                if (gui.getConfig().isSet("placeholders.secondary")) {
                    values.set(1, new String[]{gui.getConfig().getString("placeholders.secondary.start"), gui.getConfig().getString("placeholders.secondary.end")});
                }
            }
        }
        if(primary){
            return values.get(0);
        }else{
            return values.get(1);
        }
    }

    @SuppressWarnings("deprecation")
    private String dmPlaceholders(GUI gui, GUIPosition position, Player p, String identifier){

        switch(identifier){
            case("player-displayname"): {
                return p.getDisplayName();
            }
            case("player-name"): {
                return p.getName();
            }
            case("player-world"): {
                return p.getWorld().getName();
            }
            case("player-x"): {
                return String.valueOf(Math.round(p.getLocation().getX()));
            }
            case("player-y"): {
                return String.valueOf(Math.round(p.getLocation().getY()));
            }
            case("player-z"): {
                return String.valueOf(Math.round(p.getLocation().getZ()));
            }
            case("online-players"): {
                return Integer.toString(Bukkit.getServer().getOnlinePlayers().size());
            }
            case("gui-position"): {
                return position.toString();
            }
            case("tag"): {
                return plugin.tex.colour(plugin.tag);
            }
        }

        if(gui != null) {
            for (String placeholder : gui.placeholders.keys.keySet()) {
                if(identifier.equals(placeholder)) {
                    try {
                        return gui.placeholders.keys.get(placeholder);
                    } catch (Exception ex) {
                        plugin.debug(ex, p);
                        break;
                    }
                }
            }
        }

        if(identifier.startsWith("server-")) {
            String ip_port = identifier.replace("server-", "");
            Socket s = new Socket();
            try {
                s.connect(new InetSocketAddress(ip_port.split(":")[0], (int)Double.parseDouble(ip_port.split(":")[1])), plugin.config.getInt("config.server-ping-timeout"));
                s.close();
                return "true";
            }catch (IOException ex){
                return "false";
            }
        }

        if(identifier.startsWith("nbt-")) {
            try {
                String slot_key = identifier.replace("nbt-", "");
                String value;
                value = plugin.nbt.getNBT(p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(slot_key.split(":")[0])),slot_key.split(":")[1]);
                if(value == null){
                    value = "empty";
                }
                return value;
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }

        if(identifier.startsWith("material-")) {
            try {
                String matNumber = identifier.replace("material-", "");
                String material;
                try {
                    material = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getType().toString();
                    if (plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)) {
                        material = material + ":" + p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getType().getId();
                    }
                } catch (NullPointerException er) {
                    material = "AIR";
                }
                return material;
            } catch (Exception ex) {
                plugin.debug(ex,p);
                return "";
            }
        }
        if(identifier.startsWith("stack-")) {
            try {
                String matNumber = identifier.replace("stack-", "");
                int amount;
                try {
                    amount = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getAmount();
                } catch (NullPointerException er) {
                    amount = 0;
                }
                return String.valueOf(amount);
            }catch(Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        if(identifier.startsWith("modeldata-")) {
            try {
                String matNumber = identifier.replace("modeldata-", "");
                int modelData;
                try {
                    modelData = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getItemMeta().getCustomModelData();
                } catch (NullPointerException er) {
                    modelData = 0;
                }
                return String.valueOf(modelData);
            }catch(Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        if(identifier.startsWith("damaged-")) {
            try {
                String matNumber = identifier.replace("damaged-", "");
                boolean damaged = false;
                ItemStack itm = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber));
                try {
                    if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)){
                        if(itm.getType().getMaxDurability() != 0) {
                            damaged = (itm.getType().getMaxDurability() - itm.getDurability()) < itm.getType().getMaxDurability();
                        }
                    }else {
                        Damageable itemDamage = (Damageable) itm.getItemMeta();
                        damaged = itemDamage.hasDamage();
                    }
                } catch (NullPointerException er) {
                    damaged = false;
                }
                return String.valueOf(damaged);
            }catch(Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        if(identifier.startsWith("identical-")) {
            try {
                String matLocSlot = identifier.replace("identical-", "");
                String matLoc = matLocSlot.split(",")[0];
                int matSlot = (int)Double.parseDouble(matLocSlot.split(",")[1]);
                boolean isIdentical = false;
                ItemStack itm = p.getOpenInventory().getTopInventory().getItem(matSlot);

                if(itm == null){
                    return "false";
                }

                try {
                    ItemStack confItm = plugin.itemCreate.makeItemFromConfig(gui,position,gui.getConfig().getConfigurationSection("custom-item." + matLoc),p,true,true, false);
                    if(plugin.itemCreate.isIdentical(confItm,itm)){
                        isIdentical = true;
                    }

                    String customItemMaterial = gui.getConfig().getString("custom-item." + matLoc + ".material");
                    if (plugin.getServer().getPluginManager().isPluginEnabled("MMOItems") && customItemMaterial.startsWith("mmo=")) {
                        String mmoType = customItemMaterial.split("\\s")[1];
                        String mmoID = customItemMaterial.split("\\s")[2];

                        if (plugin.isMMOItem(itm,mmoType,mmoID) && itm.getAmount() <= confItm.getAmount()) {
                            isIdentical = true;
                        }
                    }
                } catch (NullPointerException er) {
                    isIdentical = false;
                }

                return String.valueOf(isIdentical);
            }catch(Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }

        if(identifier.startsWith("random-")) {
            try {
                String min_max = identifier.replace("random-", "");
                int min = (int)Double.parseDouble(min_max.split(",")[0]);
                int max = (int)Double.parseDouble(min_max.split(",")[1]);
                return String.valueOf(plugin.getRandomNumberInRange(min, max));
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        if(identifier.startsWith("data-")) {
            try {
                String dataPoint = identifier.replace("data-", "");
                if(dataPoint.contains(",")){
                    String dataName = dataPoint.split(",")[0];
                    String playerName = dataPoint.split(",")[1];
                    return plugin.guiData.getUserData(Bukkit.getOfflinePlayer(playerName).getUniqueId(),dataName);
                }else{
                    return plugin.guiData.getUserData(p.getUniqueId(),dataPoint);
                }
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }

        if(identifier.startsWith("setdata-")) {
            try {
                String point_value = identifier.replace("dm-setdata-", "");
                String command = "set-data= " + point_value.split(",")[0] + " " + point_value.split(",")[1];
                plugin.commandTags.runCommand(gui,position,p, command);
                return "";
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        if(identifier.startsWith("mathdata-")) {
            try {
                String point_value = identifier.replace("mathdata-", "");
                String command = "math-data= " + point_value.split(",")[0] + " " + point_value.split(",")[1];
                plugin.commandTags.runCommand(gui,position,p,command);
                return "";
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }

        if(identifier.startsWith("player-online-")) {
            try {
                String playerLocation = identifier.replace("player-online-", "");
                Player[] playerFind = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
                if (Double.parseDouble(playerLocation) > playerFind.length) {
                    return plugin.tex.colour(Objects.requireNonNull(plugin.config.getString("config.format.offline")));
                } else {
                    return playerFind[(int)(Double.parseDouble(playerLocation) - 1)].getName();
                }
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }

        try {
            if (plugin.econ != null) {
                if(identifier.equals("player-balance")) {
                    return String.valueOf(Math.round(plugin.econ.getBalance(p)));
                }
            }
        } catch (Exception place) {
        }
        if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
            TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
            assert api != null;
            if(identifier.equals("tokenmanager-balance")) {
                return Long.toString(api.getTokens(p).orElse(0));
            }
        }
        return "";
    }
}


