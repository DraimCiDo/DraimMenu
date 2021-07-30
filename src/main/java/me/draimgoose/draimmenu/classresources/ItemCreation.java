package me.draimgoose.draimmenu.classresources;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.ioclasses.legacy.MinecraftVersions;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemCreation {
    DraimMenu plugin;
    public ItemCreation(DraimMenu pl) {
        plugin = pl;
    }

    @SuppressWarnings("deprecation")
    public ItemStack makeItemFromConfig(GUI gui, GUIPosition position, ConfigurationSection itemSection, Player p, boolean placeholders, boolean colours, boolean addNBT){
        String material = plugin.tex.placeholdersNoColour(gui,position,p,itemSection.getString("material"));
        try {
            if (Objects.requireNonNull(material).equalsIgnoreCase("AIR")) {
                return null;
            }
        }catch(NullPointerException e){
            plugin.debug(e,p);
            p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " material: не удалось загрузить материал!"));
            return null;
        }
        ItemStack s = null;
        boolean hideAttributes = true;
        String mat;
        String matraw;
        String skullname;
        if (material.contains("%dm-player-online-")) {
            int start = material.indexOf("%dm-player-online-");
            int end = material.lastIndexOf("-find%");
            String playerLocation = material.substring(start, end).replace("%dm-player-online-", "");
            Player[] playerFind = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
            if (Integer.parseInt(playerLocation) > playerFind.length) {
                material = material.replace(material.substring(start, end) + "-find%", "dms= " + plugin.config.getString("config.format.offlineHeadValue"));
            } else {
                material = material.replace(material.substring(start, end) + "-find%", "dmo= " + playerFind[Integer.parseInt(playerLocation) - 1].getName());
            }
        }
        try {
            mat = material.toUpperCase();
            matraw = material;
            boolean normalCreation = true;
            skullname = "нет головы";
            short id = 0;
            if(itemSection.contains("ID")){
                id = Short.parseShort(itemSection.getString("ID"));
            }
            if (matraw.split("\\s")[0].equalsIgnoreCase("dms=") || matraw.split("\\s")[0].toLowerCase().equals("dmo=")) {
                skullname = p.getUniqueId().toString();
                mat = plugin.getHeads.playerHeadString();
                if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
                    id = 3;
                }
            }

            if (matraw.split("\\s")[0].equalsIgnoreCase("hdb=")) {
                skullname = "hdb";
                mat = plugin.getHeads.playerHeadString();
                if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
                    id = 3;
                }
            }

            if(matraw.split("\\s")[0].equalsIgnoreCase("book=")){
                s = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta bookMeta = (BookMeta) s.getItemMeta();
                bookMeta.setTitle(matraw.split("\\s")[1]);
                bookMeta.setAuthor(matraw.split("\\s")[1]);
                List<String> bookLines = plugin.tex.placeholdersList(gui,position,p,itemSection.getStringList("write"),true);
                String result = bookLines.stream().map(String::valueOf).collect(Collectors.joining("\n" + ChatColor.RESET, "", ""));
                bookMeta.setPages(result);
                s.setItemMeta(bookMeta);
                normalCreation = false;
            }

            if(matraw.split("\\s")[0].equalsIgnoreCase("dmi=")){
                s = makeCustomItemFromConfig(gui,position,gui.getConfig().getConfigurationSection("custom-item." + matraw.split("\\s")[1]), p, true, true, true);
                normalCreation = false;
            }

            if(normalCreation) {
                s = new ItemStack(Objects.requireNonNull(Material.matchMaterial(mat)), 1, id);
            }

            if (!skullname.equals("нет головы") && !skullname.equals("hdb") && !matraw.split("\\s")[0].equalsIgnoreCase("dmo=")) {
                try {
                    SkullMeta meta;
                    if (matraw.split("\\s")[1].equalsIgnoreCase("self")) {
                        meta = (SkullMeta) s.getItemMeta();
                        if(!plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)) {
                            try {
                                assert meta != null;
                                meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(skullname)));
                            } catch (Exception var23) {
                                p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " material: dms= self"));
                                plugin.debug(var23,p);
                            }
                        }else{
                            meta.setOwner(p.getName());
                        }
                        s.setItemMeta(meta);
                    }else if (plugin.tex.placeholdersNoColour(gui,position,p,matraw.split("\\s")[1]).length() <= 16) {
                        s = plugin.customHeads.getPlayerHead(plugin.tex.placeholdersNoColour(gui,position,p,matraw.split("\\s")[1]));
                    } else {
                        s = plugin.customHeads.getCustomHead(plugin.tex.placeholdersNoColour(gui,position,p,matraw.split("\\s")[1]));
                    }
                } catch (Exception var32) {
                    p.sendMessage(plugin.tex.colour( plugin.tag + plugin.config.getString("config.format.error") + " head material: Не удалось загрузить голову!"));
                    plugin.debug(var32,p);
                }
            }
            if (!skullname.equals("нет головы") && matraw.split("\\s")[0].equalsIgnoreCase("dmo=")) {
                SkullMeta cpoMeta = (SkullMeta) s.getItemMeta();
                assert cpoMeta != null;
                cpoMeta.setOwningPlayer(Bukkit.getOfflinePlayer(Objects.requireNonNull(Bukkit.getPlayer(matraw.split("\\s")[1])).getUniqueId()));
                s.setItemMeta(cpoMeta);
            }
            if (skullname.equals("hdb")) {
                if (plugin.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
                    HeadDatabaseAPI api;
                    api = new HeadDatabaseAPI();

                    try {
                        s = api.getItemHead(matraw.split("\\s")[1].trim());
                    } catch (Exception var22) {
                        p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " hdb: Не удалось загрузить голову!"));
                        plugin.debug(var22,p);
                    }
                } else {
                    p.sendMessage(plugin.tex.colour(plugin.tag + "Скачайте HeadDatabaseHook из крана, чтобы использовать эту функцию!"));
                }
            }

            if(itemSection.contains("itemType")){
                if(itemSection.getStringList("itemType").contains("noAttributes")){
                    hideAttributes = false;
                }
                if(itemSection.getStringList("itemType").contains("noNBT")){
                    addNBT = false;
                }
                if(itemSection.getStringList("itemType").contains("placeable")){
                    addNBT = false;
                }
            }

            if(addNBT){
                s = plugin.nbt.setNBT(s);
            }

            if (itemSection.contains("map")) {
                try{
                    @SuppressWarnings("deprecation")
                    MapView map = Bukkit.getServer().getMap(0);
                    try {
                        map.getRenderers().clear();
                        map.setCenterX(30000000);
                        map.setCenterZ(30000000);
                    }catch(NullPointerException ignore){
                    }
                    if(new File(plugin.getDataFolder().getPath() + File.separator + "maps" + File.separator + itemSection.getString("map")).exists()) {
                        map.addRenderer(new MapRenderer() {
                            public void render(MapView view, MapCanvas canvas, Player player) {
                                canvas.drawImage(0, 0, new ImageIcon(plugin.getDataFolder().getPath() + File.separator + "maps" + File.separator + itemSection.getString("map")).getImage());
                            }
                        });
                        MapMeta meta = (MapMeta) s.getItemMeta();
                        meta.setMapView(map);
                        s.setItemMeta(meta);
                    }else{
                        p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " map: Файл не найден."));
                    }
                }catch(Exception map){
                    p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " map: " + itemSection.getString("map")));
                    plugin.debug(map,p);
                }
            }
            if (itemSection.contains("enchanted")) {
                try {
                    ItemMeta EnchantMeta;
                    if (Objects.requireNonNull(itemSection.getString("enchanted")).trim().equalsIgnoreCase("true")) {
                        EnchantMeta = s.getItemMeta();
                        assert EnchantMeta != null;
                        EnchantMeta.addEnchant(Enchantment.KNOCKBACK, 1, false);
                        EnchantMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        s.setItemMeta(EnchantMeta);
                    } else if (!Objects.requireNonNull(itemSection.getString("enchanted")).trim().equalsIgnoreCase("false")) {
                        EnchantMeta = s.getItemMeta();
                        assert EnchantMeta != null;
                        EnchantMeta.addEnchant(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(Objects.requireNonNull(itemSection.getString("enchanted")).split("\\s")[0].toLowerCase()))), Integer.parseInt(Objects.requireNonNull(itemSection.getString("enchanted")).split("\\s")[1]), true);
                        s.setItemMeta(EnchantMeta);
                    }
                } catch (Exception ench) {
                    p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " enchanted: " + itemSection.getString("enchanted")));
                    plugin.debug(ench,p);
                }
            }
            if (itemSection.contains("customdata")) {
                ItemMeta customMeta = s.getItemMeta();
                assert customMeta != null;
                customMeta.setCustomModelData(Integer.parseInt(plugin.tex.placeholders(gui,position,p,itemSection.getString("customdata"))));
                s.setItemMeta(customMeta);
            }
            try {
                if (itemSection.contains("banner")) {
                    BannerMeta bannerMeta = (BannerMeta) s.getItemMeta();
                    List<Pattern> patterns = new ArrayList<>();
                    for (String temp : itemSection.getStringList("banner")) {
                        String[] dyePattern = temp.split(",");
                        patterns.add(new Pattern(DyeColor.valueOf(dyePattern[0]), PatternType.valueOf(dyePattern[1])));
                    }
                    bannerMeta.setPatterns(patterns);
                    bannerMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                    s.setItemMeta(bannerMeta);
                }
            }catch(Exception ignore){
            }
            if (itemSection.contains("leatherarmor")) {
                try {
                    if (s.getType() == Material.LEATHER_BOOTS || s.getType() == Material.LEATHER_LEGGINGS || s.getType() == Material.LEATHER_CHESTPLATE || s.getType() == Material.LEATHER_HELMET) {
                        LeatherArmorMeta leatherMeta = (LeatherArmorMeta) s.getItemMeta();
                        String colourCode = itemSection.getString("leatherarmor");
                        assert colourCode != null;
                        if (!colourCode.contains(",")) {
                            assert leatherMeta != null;
                            leatherMeta.setColor(plugin.colourCodes.get(colourCode.toUpperCase()));
                        } else {
                            int[] colorRGB = {255, 255, 255};
                            int count = 0;
                            for (String colourNum : colourCode.split(",")) {
                                colorRGB[count] = Integer.parseInt(colourNum);
                                count += 1;
                            }
                            assert leatherMeta != null;
                            leatherMeta.setColor(Color.fromRGB(colorRGB[0], colorRGB[1], colorRGB[2]));
                        }
                        s.setItemMeta(leatherMeta);
                    }
                } catch (Exception er) {
                    plugin.debug(er,p);
                    p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " leatherarmor: " + itemSection.getString("leatherarmor")));
                }
            }

            if (itemSection.contains("potion")) {
                try {
                    PotionMeta potionMeta = (PotionMeta)s.getItemMeta();
                    String effectType = itemSection.getString("potion");
                    assert potionMeta != null;
                    assert effectType != null;
                    potionMeta.setBasePotionData(new PotionData(PotionType.valueOf(effectType.toUpperCase())));
                    potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                    s.setItemMeta(potionMeta);
                } catch (Exception er) {
                    plugin.debug(er,p);
                    p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + plugin.config.getString("config.format.error") + " potion: " + itemSection.getString("potion")));
                }
            }
            if (itemSection.contains("damage")) {
                if (plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)) {
                    try {
                        s.setDurability(Short.parseShort(Objects.requireNonNull(plugin.tex.placeholders(gui,position,p, itemSection.getString("damage")))));
                    } catch (Exception e) {
                        plugin.debug(e, p);
                        p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " damage: " + itemSection.getString("damage")));
                    }
                } else {
                    if(itemSection.getString("damage").equalsIgnoreCase("-1")){
                        ItemMeta unbreak = s.getItemMeta();
                        unbreak.setUnbreakable(true);
                        s.setItemMeta(unbreak);
                    }

                    try {
                        Damageable itemDamage = (Damageable) s.getItemMeta();
                        itemDamage.setDamage(Integer.parseInt(Objects.requireNonNull(plugin.tex.placeholders(gui,position,p, itemSection.getString("damage")))));
                        s.setItemMeta((ItemMeta) itemDamage);
                    } catch (Exception e) {
                        plugin.debug(e, p);
                        p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " damage: " + itemSection.getString("damage")));
                    }
                }
            }
            if (itemSection.contains("nbt")) {
                for(String key : itemSection.getConfigurationSection("nbt").getKeys(false)){
                    s = plugin.nbt.setNBT(s,key,itemSection.getString("nbt." + key));
                }
            }
            if (itemSection.contains("stack")) {
                s.setAmount((int)Double.parseDouble(Objects.requireNonNull(plugin.tex.placeholders(gui,position,p,itemSection.getString("stack")))));
            }
        } catch (IllegalArgumentException | NullPointerException var33) {
            plugin.debug(var33,p);
            p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " material: " + itemSection.getString("material")));
            return null;
        }
        plugin.setName(gui,s, itemSection.getString("name"), itemSection.getStringList("lore"), p, placeholders, colours, hideAttributes);
        return s;
    }

    public ItemStack makeCustomItemFromConfig(GUI gui,GUIPosition position, ConfigurationSection itemSection, Player p, boolean placeholders, boolean colours, boolean addNBT){
        String section = plugin.itemCreate.hasSection(gui,position,itemSection,p);
        if(!section.equals("")){
            itemSection = itemSection.getConfigurationSection(section.substring(1));
        }
        return plugin.itemCreate.makeItemFromConfig(gui,position,itemSection, p, placeholders, colours, addNBT);
    }

    public String hasSection(GUI gui,GUIPosition position, ConfigurationSection cf, Player p){
        if (cf.isSet("hasvalue")) {
            boolean outputValue = true;
            if (cf.contains("hasvalue.output")) {
                outputValue = cf.getBoolean("hasvalue.output");
            }
            String value = ChatColor.stripColor(plugin.tex.placeholders(gui,position,p,cf.getString("hasvalue.value")));
            String compare = ChatColor.stripColor(plugin.tex.placeholders(gui,position,p,cf.getString("hasvalue.compare")));
            if (compare.equals(value) == outputValue) {
                String section = hasSection(gui,position,Objects.requireNonNull(cf.getConfigurationSection("hasvalue")), p);
                return ".hasvalue" + section;
            }
            for (int count = 0; cf.getKeys(false).size() > count; count++) {
                if (cf.contains("hasvalue" + count)) {
                    outputValue = true;
                    if (cf.contains("hasvalue" + count + ".output")) {
                        outputValue = cf.getBoolean("hasvalue" + count + ".output");
                    }
                    value = ChatColor.stripColor(plugin.tex.placeholders(gui,position,p,cf.getString("hasvalue" + count + ".value")));
                    compare = ChatColor.stripColor(plugin.tex.placeholders(gui,position,p,cf.getString("hasvalue" + count + ".compare")));
                    if (compare.equals(value) == outputValue) {
                        String section = hasSection(gui,position,Objects.requireNonNull(cf.getConfigurationSection("hasvalue" + count)), p);
                        return ".hasvalue" + count + section;
                    }
                }
            }
        }
        if (cf.isSet("hasgreater")) {
            boolean outputValue = true;
            if (cf.contains("hasgreater.output")) {
                outputValue = cf.getBoolean("hasgreater.output");
            }
            double value = Double.parseDouble(ChatColor.stripColor(plugin.tex.placeholdersNoColour(gui,position,p,cf.getString("hasgreater.value"))));
            double compare = Double.parseDouble(ChatColor.stripColor(plugin.tex.placeholdersNoColour(gui,position,p,cf.getString("hasgreater.compare"))));
            if ((compare >= value) == outputValue) {
                String section = hasSection(gui,position,Objects.requireNonNull(cf.getConfigurationSection("hasgreater")), p);
                return ".hasgreater" + section;
            }
            for (int count = 0; cf.getKeys(false).size() > count; count++) {
                if (cf.contains("hasgreater" + count)) {
                    outputValue = true;
                    if (cf.contains("hasgreater" + count + ".output")) {
                        outputValue = cf.getBoolean("hasgreater" + count + ".output");
                    }
                    value = Double.parseDouble(ChatColor.stripColor(plugin.tex.placeholdersNoColour(gui,position,p,cf.getString("hasgreater" + count + ".value"))));
                    compare = Double.parseDouble(ChatColor.stripColor(plugin.tex.placeholdersNoColour(gui,position,p,cf.getString("hasgreater" + count + ".compare"))));
                    if ((compare >= value) == outputValue) {
                        String section = hasSection(gui,position,Objects.requireNonNull(cf.getConfigurationSection("hasgreater" + count)), p);
                        return ".hasgreater" + count + section;
                    }
                }
            }
        }
        if (cf.isSet("hasperm")) {
            boolean outputValue = true;
            if (cf.contains("hasperm.output")) {
                outputValue = cf.getBoolean("hasperm.output");
            }
            if (p.hasPermission(Objects.requireNonNull(cf.getString("hasperm.perm"))) == outputValue) {
                String section = hasSection(gui,position,Objects.requireNonNull(cf.getConfigurationSection("hasperm")), p);
                return ".hasperm" + section;
            }
            for(int count = 0; cf.getKeys(false).size() > count; count++){
                if (cf.contains("hasperm" + count) && cf.contains("hasperm"  + count + ".perm")) {
                    outputValue = true;
                    if (cf.contains("hasperm" + count + ".output")) {
                        outputValue = cf.getBoolean("hasperm" + count + ".output");
                    }
                    if (p.hasPermission(Objects.requireNonNull(cf.getString("hasperm" + count + ".perm"))) == outputValue) {
                        String section = hasSection(gui,position,Objects.requireNonNull(cf.getConfigurationSection("hasperm" + count)), p);
                        return ".hasperm" + count + section;
                    }
                }
            }
        }
        return "";
    }

    @SuppressWarnings("deprecation")
    public YamlConfiguration generateGUIFile(String guiName, Inventory inv, YamlConfiguration file){
        ItemStack cont;
        for(int i = 0; inv.getSize() > i; i++){
            cont = inv.getItem(i);
            try{
                if(cont == null){
                    if(file.contains("guis." + guiName + ".item." + i)){
                        if(!file.getString("guis." + guiName + ".item." + i + ".material").equalsIgnoreCase("AIR")) {
                            file.set("guis." + guiName + ".item." + i, null);
                            continue;
                        }
                    }
                }
                if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
                    if (cont.getDurability() != 0 && !cont.getType().toString().equals("SKULL_ITEM")) {
                        file.set("guis." + guiName + ".item." + i + ".ID", cont.getDurability());
                    }
                }
                if(file.contains("guis." + guiName + ".item." + i + ".material")){
                    if(Objects.requireNonNull(file.getString("guis." + guiName + ".item." + i + ".material")).contains("%") || Objects.requireNonNull(file.getString("guis." + guiName + ".item." + i + ".material")).contains("=")){
                        if(!plugin.getHeads.ifSkullOrHead(cont.getType().toString())){
                            file.set("guis." + guiName + ".item." + i + ".material", cont.getType().toString());
                        }
                    }else{
                        file.set("guis." + guiName + ".item." + i + ".material", cont.getType().toString());
                    }
                }else{
                    file.set("guis." + guiName + ".item." + i + ".material", cont.getType().toString());
                }
                if(plugin.getHeads.ifSkullOrHead(cont.getType().toString())){
                    if(!Objects.requireNonNull(file.getString("guis." + guiName + ".item." + i + ".material")).contains("%") && !Objects.requireNonNull(file.getString("guis." + guiName + ".item." + i + ".material")).contains("=")) {
                        SkullMeta meta = (SkullMeta) cont.getItemMeta();
                        if (plugin.customHeads.getHeadBase64(cont) != null && !plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)) {
                            file.set("guis." + guiName + ".item." + i + ".material", "dms= " + plugin.customHeads.getHeadBase64(cont));
                        } else if (meta.hasOwner()) {
                            file.set("guis." + guiName + ".item." + i + ".material", "dms= " + meta.getOwner());
                        }
                    }
                }
                try {
                    BannerMeta bannerMeta = (BannerMeta) cont.getItemMeta();
                    List<String> dyePattern = new ArrayList<>();
                    for(Pattern pattern : bannerMeta.getPatterns()) {
                        dyePattern.add(pattern.getColor().toString() + "," + pattern.getPattern().toString());
                    }
                    file.set("guis." + guiName + ".item." + i + ".banner", dyePattern);
                }catch(Exception ignore){
                    file.set("guis." + guiName + ".item." + i + ".banner", null);
                }
                file.set("guis." + guiName + ".item." + i + ".stack", cont.getAmount());
                if(!cont.getEnchantments().isEmpty()){
                    file.set("guis." + guiName + ".item." + i + ".enchanted", "true");
                }
                file.set("guis." + guiName + ".item." + i + ".name", Objects.requireNonNull(cont.getItemMeta()).getDisplayName());
                file.set("guis." + guiName + ".item." + i + ".lore", Objects.requireNonNull(cont.getItemMeta()).getLore());
            }catch(Exception n){
            }
        }
        return file;
    }

    @SuppressWarnings("deprecation")
    public boolean isIdentical(ItemStack one, ItemStack two){
        if (one.getType() != two.getType()) {
            return false;
        }
        try {
            if (!one.getItemMeta().getDisplayName().equals(two.getItemMeta().getDisplayName())) {
                if(one.getItemMeta().hasDisplayName()) {
                    return false;
                }
            }
        }catch(Exception ignore){}
        try {
            if (!one.getItemMeta().getLore().equals(two.getItemMeta().getLore())) {
                if(one.getItemMeta().hasLore()) {
                    return false;
                }
            }
        }catch(Exception ignore){}
        try {
            if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
                if(one.getDurability() != two.getDurability()) {
                    return false;
                }
            }else {
                Damageable tempOne = (Damageable) one.getItemMeta();
                Damageable tempTwo = (Damageable) two.getItemMeta();
                if(tempOne.getDamage() != tempTwo.getDamage()){
                    return false;
                }
            }
        } catch (Exception ignore) {}
        if(one.getEnchantments() == two.getEnchantments()){
            if(!one.getEnchantments().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
