package me.draimgoose.draimmenu.ingameeditor;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.openguimanager.GUIOpenType;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditorUserInput implements Listener {
    DraimMenu plugin;
    public EditorUserInput(DraimMenu pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onPlayerChatEditor(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        for(String[] temp : plugin.editorInputStrings) {
            if (!temp[0].equals(p.getName())) {
                continue;
            }
            e.setCancelled(true);
            String guiName = temp[1];
            String guiTitle = temp[1];
            File guiFile = null;
            String section = temp[2];
            YamlConfiguration cfile = null;
            ConfigurationSection cf = null;
            try {
                for (GUI gui : plugin.guiList) {
                    if (gui.getName().equals(guiName)) {
                        cf = gui.getConfig();
                        cfile = YamlConfiguration.loadConfiguration(gui.getFile());
                        guiFile = gui.getFile();
                        guiTitle = plugin.tex.colour(cf.getString("title"));
                        break;
                    }
                }
            } catch (Exception fail) {
                plugin.debug(fail,p);
                plugin.editorInputStrings.remove(temp);
                return;
            }
            if(e.getMessage().equalsIgnoreCase(plugin.config.getString("config.input-cancel"))){
                plugin.editorInputStrings.remove(temp);
                plugin.reloadGUIFiles();
                e.getPlayer().sendMessage(plugin.tex.colour( Objects.requireNonNull(plugin.config.getString("config.input-cancelled"))));
                return;
            }
            plugin.editorInputStrings.remove(temp);
            if(section.startsWith("gui.")) {
                guiSectionCheck(p, section, guiName, guiTitle, cf, cfile, guiFile, e);
            }else if(section.startsWith("item:")){
                itemSectionCheck(p, section, guiName, cf, cfile, guiFile, e);
            }else if(section.startsWith("section.")){
                itemSectionSectionCheck(p, section, guiName, cf, cfile, guiFile, e);
            }
            plugin.reloadGUIFiles();
            if(section.startsWith("gui.")){
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        plugin.editorGuis.openEditorGUI(p, 0);
                    }
                });
            }else if(section.startsWith("item:")) {
                final ConfigurationSection finalCF = cf;
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        plugin.createGUI.openGui(new GUI(finalCF, guiName), p, GUIPosition.Top, GUIOpenType.Editor,0);
                    }
                });
            }else if(section.startsWith("section.")){
                String itemSection = ChatColor.stripColor(section.replace("section." + section.split("\\.")[1] + ".", ""));
                final ConfigurationSection finalCF = cf.getConfigurationSection("item." + itemSection);
                if(section.contains("change")){
                    final String changeItemSection = itemSection.substring(0, itemSection.lastIndexOf("."));
                    final ConfigurationSection changeFinalCF = cf.getConfigurationSection("item." + changeItemSection);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            plugin.editorGuis.openItemSections(p,guiName,changeFinalCF,changeItemSection);
                        }
                    });
                }else{
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            plugin.editorGuis.openItemSections(p,guiName,finalCF,itemSection);
                        }
                    });
                }
            }
            return;
        }
    }
    boolean saveGUIFile(ConfigurationSection cf, YamlConfiguration cfile, String guiName, File guiFile){
        try {
            cfile.set("gui." + guiName, cf);
            cfile.save(guiFile);
            return true;
        } catch (Exception io) {
            plugin.debug(io,null);
            return false;
        }
    }

    void guiSectionCheck(Player p, String section, String guiName, String guiTitle, ConfigurationSection cf, YamlConfiguration cfile, File guiFile, AsyncPlayerChatEvent e){
        switch (section) {
            case "gui.delete":
                if (e.getMessage().contains("y")) {
                    if(Objects.requireNonNull(cfile.getConfigurationSection("guis")).getKeys(false).size() != 1){
                        if(saveGUIFile(null, cfile, guiName, guiFile)){
                            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Меню удалено"));
                        }else{
                            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не удалось удалить меню!"));
                        }
                    }else {
                        if (guiFile.delete()) {
                            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Меню удалено!"));
                        }else{
                            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не удалось удалить меню!"));
                        }
                    }
                }
                break;
            case "gui.perm":
                if(e.getMessage().contains(" ")){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Разрешение не может содержать пробелов!"));
                    break;
                }
                cf.set("perm", e.getMessage());
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Требуется разрешение " + "draimmenu.gui." + e.getMessage()));
                break;
            case "gui.rows":
                try {
                    int rows = Integer.parseInt(e.getMessage());
                    if (rows >= 7 || rows <= 0) {
                        p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Выберите целое число от 1 до 6!"));
                        return;
                    }
                    cf.set("rows", rows);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлено меню на " + rows + " строк!"));
                } catch (Exception io) {
                    plugin.debug(io,p);
                }
                break;
            case "gui.title":
                if(guiTitle.equals(plugin.tex.colour(e.getMessage()))){
                    p.sendMessage(plugin.tex.colour(plugin.tag + e.getMessage() + ChatColor.RED + " используется с другой менюшкой!"));
                    break;
                }
                cf.set("title", e.getMessage());
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлено новое название на " + ChatColor.WHITE + e.getMessage()));
                break;
            case "gui.name":
                if(e.getMessage().contains(" ")){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Имя панели не может содержать пробелов!"));
                    break;
                }
                if(guiName.equals(e.getMessage())){
                    p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + e.getMessage() + " используется с другим меню!"));
                    break;
                }
                cfile.set("gui." + e.getMessage(), cfile.get("gui." + guiName));
                saveGUIFile(null, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлено новое имя на " + e.getMessage()));
                break;
            case "gui.empty":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("empty", null);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Пустые предметы были удалены."));
                    break;
                }
                String materialTemp = null;
                try {
                    materialTemp = Objects.requireNonNull(Material.matchMaterial(e.getMessage())).toString();
                }catch(NullPointerException ex){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + e.getMessage() + " не является действительным предметом!"));
                }
                cf.set("empty", materialTemp);
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлен пустой предмет на " + materialTemp));
                break;
            case "gui.sound-on-open":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("sound-on-open", null);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Звуки были удалены."));
                    break;
                }
                String tempSound;
                try {
                    tempSound = Sound.valueOf(e.getMessage()).toString();
                }catch(Exception ex){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + e.getMessage() + " это недопустимый звук!"));
                    return;
                }
                cf.set("sound-on-open", tempSound);
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Звук при открытии теперь " + tempSound));
                break;
            case "gui.guiType.add":
                List<String> typeAdd = new ArrayList<>();
                if(cf.contains("guiType")){
                    typeAdd = cf.getStringList("guiType");
                }
                typeAdd.add(e.getMessage().toLowerCase());
                cf.set("guiType", typeAdd);
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Добавлен новый тип панели: " + e.getMessage().toLowerCase()));
                break;
            case "gui.guiType.remove":
                List<String> typeRemove;
                if(cf.contains("guiType")){
                    typeRemove = cf.getStringList("guiType");
                }else{
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не найдено типов для удаления!"));
                    break;
                }
                try {
                    typeRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не удалось найти тип!"));
                    break;
                }
                if(typeRemove.size() == 0){
                    cf.set("guiType", null);
                }else{
                    cf.set("guiType", typeRemove);
                }
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Удален тип " + e.getMessage()));
                break;
            case "gui.commands.add":
                List<String> commandsAdd = new ArrayList<>();
                if(cf.contains("commands")){
                    commandsAdd = cf.getStringList("commands");
                }
                commandsAdd.add(e.getMessage());
                cf.set("commands", commandsAdd);
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Добавлена новая команда: " + e.getMessage()));
                break;
            case "gui.commands.remove":
                List<String> commandsRemove;
                if(cf.contains("commands")){
                    commandsRemove = cf.getStringList("commands");
                }else{
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не найдено команд для удаления!"));
                    break;
                }
                try {
                    commandsRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не смог найти команду!"));
                    break;
                }
                if(commandsRemove.size() == 0){
                    cf.set("commands", null);
                }else{
                    cf.set("commands", commandsRemove);
                }
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Удалена команда " + e.getMessage()));
                break;
            case "gui.commands-on-open.add":
                List<String> commandsOnOpenAdd = new ArrayList<>();
                if(cf.contains("commands-on-open")){
                    commandsOnOpenAdd = cf.getStringList("commands-on-open");
                }
                commandsOnOpenAdd.add(e.getMessage());
                cf.set("commands-on-open", commandsOnOpenAdd);
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Добавлена новая команда: " + e.getMessage()));
                break;
            case "gui.commands-on-open.remove":
                List<String> commandsOnOpenRemove;
                if(cf.contains("commands-on-open")){
                    commandsOnOpenRemove = cf.getStringList("commands-on-open");
                }else{
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не найдено команд для удаления!"));
                    break;
                }
                try {
                    commandsOnOpenRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не удалось найти команду!"));
                    break;
                }
                if(commandsOnOpenRemove.size() == 0){
                    cf.set("commands-on-open", null);
                }else{
                    cf.set("commands-on-open", commandsOnOpenRemove);
                }
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Удалена команда " + e.getMessage()));
                break;
            case "gui.disabled-worlds.add":
                List<String> disabledWorldsAdd = new ArrayList<>();
                if(cf.contains("disabled-worlds")){
                    disabledWorldsAdd = cf.getStringList("disabled-worlds");
                }
                disabledWorldsAdd.add(e.getMessage());
                cf.set("disabled-worlds", disabledWorldsAdd);
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Добавлен новый мир: " + e.getMessage()));
                break;
            case "gui.disabled-worlds.remove":
                List<String> disabledWorldsRemove;
                if(cf.contains("disabled-worlds")){
                    disabledWorldsRemove = cf.getStringList("disabled-worlds");
                }else{
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не найдено миров для удаления!"));
                    break;
                }
                try {
                    disabledWorldsRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не мог найти мир!"));
                    break;
                }
                if(disabledWorldsRemove.size() == 0){
                    cf.set("disabled-worlds", null);
                }else{
                    cf.set("disabled-worlds", disabledWorldsRemove);
                }
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Удален мир " + e.getMessage()));
                break;
            case "gui.hotbar.material":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("open-with-item", null);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Хотбар предмет был удален."));
                    plugin.reloadGUIFiles();
                    break;
                }
                cf.set("open-with-item.material", e.getMessage());
                if(!cf.contains("open-with-item.name")){
                    cf.set("open-with-item.name", guiName + " Item");
                }
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлен новый предмет на " + ChatColor.WHITE + e.getMessage()));
                plugin.reloadGUIFiles();
                break;
            case "gui.hotbar.stationary":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("open-with-item.stationary", null);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Теперь хотбар предмет можно перемещать."));
                    break;
                }
                try {
                    int loc = Integer.parseInt(e.getMessage());
                    if (loc >= 36 || loc <= -1) {
                        p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Выберите целое число от 0 до 35!"));
                        return;
                    }
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлено расположение хотбар предмета на " + loc + "!"));
                    cf.set("open-with-item.stationary", loc);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                } catch (Exception io) {
                    plugin.debug(io,p);
                }
                plugin.hotbar.reloadHotbarSlots();
                break;
            case "gui.hotbar.name":
                cf.set("open-with-item.name",e.getMessage());
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установите новое имя на " + ChatColor.WHITE + e.getMessage()));
                break;
            case "gui.hotbar.lore.add":
                List<String> loreAdd = new ArrayList<>();
                if(cf.contains("open-with-item.lore")){
                    loreAdd = cf.getStringList("open-with-item.lore");
                }
                loreAdd.add(e.getMessage());
                cf.set("open-with-item.lore", loreAdd);
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Добавлено новое описание: " + e.getMessage()));
                break;
            case "gui.hotbar.lore.remove":
                List<String> loreRemove;
                if(cf.contains("open-with-item.lore")){
                    loreRemove = cf.getStringList("open-with-item.lore");
                }else{
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не найдено описание, которые можно было бы удалить!"));
                    break;
                }
                try {
                    loreRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не смог найти описание!"));
                    break;
                }
                if(loreRemove.size() == 0){
                    cf.set("open-with-item.lore", null);
                }else{
                    cf.set("open-with-item.lore", loreRemove);
                }
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Удалена строка описания " + e.getMessage()));
                break;
            case "gui.hotbar.commands.add":
                List<String> commandAdd = new ArrayList<>();
                if(cf.contains("open-with-item.commands")){
                    commandAdd = cf.getStringList("open-with-item.commands");
                }
                commandAdd.add(e.getMessage());
                cf.set("open-with-item.commands", commandAdd);
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Добавлена новая команда: " + e.getMessage()));
                break;
            case "gui.hotbar.commands.remove":
                List<String> commandRemove;
                if(cf.contains("open-with-item.commands")){
                    commandRemove = cf.getStringList("open-with-item.commands");
                }else{
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не найдено команд для удаления!"));
                    break;
                }
                try {
                    commandRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не смог найти команду!"));
                    break;
                }
                if(commandRemove.size() == 0){
                    cf.set("open-with-item.commands", null);
                }else{
                    cf.set("open-with-item.commands", commandRemove);
                }
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Удалена команда " + e.getMessage()));
                break;
        }
    }

    void itemSectionCheck(Player p, String section, String guiName, ConfigurationSection cf, YamlConfiguration cfile, File guiFile, AsyncPlayerChatEvent e){
        String itemSlot = section.split(":")[1];
        String sectionChange = section.replace("item:" + itemSlot + ":","");
        switch (sectionChange) {
            case "name":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".name", "");
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Имя установлено по умолчанию."));
                    break;
                }
                cf.set("item." + itemSlot + ".name", e.getMessage());
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлено новое имя на " + ChatColor.WHITE + e.getMessage()));
                break;
            case "head":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".material", plugin.getHeads.playerHeadString());
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Предмет теперь используется по умолчанию."));
                    break;
                }
                cf.set("item." + itemSlot + ".material", e.getMessage());
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлено значение предмета равным " + ChatColor.WHITE + e.getMessage()));
                break;
            case "stack":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".stack", null);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Предмет стал одиночным."));
                    break;
                }
                try {
                    int rows = Integer.parseInt(e.getMessage());
                    if (rows >= 65 || rows <= 0) {
                        p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Выберите целое число от 1 до 64!"));
                        return;
                    }
                    cf.set("item." + itemSlot + ".stack", rows);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлено значение на " + rows + "!"));
                } catch (Exception io) {
                    plugin.debug(io,p);
                }
                break;
            case "enchanted":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".enchanted", null);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Чары были сняты."));
                    break;
                }
                cf.set("item." + itemSlot + ".enchanted", e.getMessage());
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлено новое заклинание " + ChatColor.WHITE + e.getMessage()));
                break;
            case "potion":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".potion", null);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Эффекты зелий были удалены."));
                    break;
                }
                cf.set("item." + itemSlot + ".potion", e.getMessage());
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлено новое зелье " + e.getMessage().toUpperCase()));
                break;
            case "customdata":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".customdata", null);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Кастомный пердмет был удален."));
                    break;
                }
                cf.set("item." + itemSlot + ".customdata", e.getMessage());
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Кастомный предмет установлен на " + e.getMessage()));
                break;
            case "leatherarmor":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".leatherarmor", null);
                    saveGUIFile(cf, cfile, guiName, guiFile);
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Цвет кожаной брони был удален."));
                    break;
                }
                cf.set("item." + itemSlot + ".leatherarmor", e.getMessage());
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Цвет кожаной брони установлен на " + e.getMessage()));
                break;
            case "commands:add":
                List<String> commandsOnOpenAdd = new ArrayList<>();
                if(cf.contains("item." + itemSlot + ".commands")){
                    commandsOnOpenAdd = cf.getStringList("item." + itemSlot + ".commands");
                }
                commandsOnOpenAdd.add(e.getMessage());
                cf.set("item." + itemSlot + ".commands", commandsOnOpenAdd);
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Добавлена новая команда: " + e.getMessage()));
                break;
            case "commands:remove":
                List<String> commandsOnOpenRemove;
                if(cf.contains("item." + itemSlot + ".commands")){
                    commandsOnOpenRemove = cf.getStringList("item." + itemSlot + ".commands");
                }else{
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не найдено команд для удаления!"));
                    break;
                }
                try {
                    commandsOnOpenRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не удалось найти команду!"));
                    plugin.debug(ex,p);
                    break;
                }
                if(commandsOnOpenRemove.size() == 0){
                    cf.set("item." + itemSlot + ".commands", null);
                }else{
                    cf.set("item." + itemSlot + ".commands", commandsOnOpenRemove);
                }
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Удалена команда " + e.getMessage()));
                break;
            case "lore:add":
                List<String> loreOnOpenAdd = new ArrayList<>();
                if(cf.contains("item." + itemSlot + ".lore")){
                    loreOnOpenAdd = cf.getStringList("item." + itemSlot + ".lore");
                }
                loreOnOpenAdd.add(e.getMessage());
                cf.set("item." + itemSlot + ".lore", loreOnOpenAdd);
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Добавлено новое описание: " + e.getMessage()));
                break;
            case "lore:remove":
                List<String> loreOnOpenRemove;
                if(cf.contains("item." + itemSlot + ".lore")){
                    loreOnOpenRemove = cf.getStringList("item." + itemSlot + ".lore");
                }else{
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не найдено описание, которые можно было бы удалить!"));
                    break;
                }
                try {
                    loreOnOpenRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не смог найти описание!"));
                    plugin.debug(ex,p);
                    break;
                }
                if(loreOnOpenRemove.size() == 0){
                    cf.set("item." + itemSlot + ".lore", null);
                }else{
                    cf.set("item." + itemSlot + ".lore", loreOnOpenRemove);
                }
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Удалена строка описания " + e.getMessage()));
                break;
            case "duplicate:add":
                if(cf.contains("item." + itemSlot + ".duplicate")){
                    cf.set("item." + itemSlot + ".duplicate", cf.getString("item." + itemSlot + ".duplicate") + "," + e.getMessage());
                }else{
                    cf.set("item." + itemSlot + ".duplicate", e.getMessage());
                }
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Добавлен новый дубликат предмета: " + e.getMessage()));
                break;
            case "duplicate:remove":
                if(cf.contains("item." + itemSlot + ".duplicate")){
                    if(cf.getString("item." + itemSlot + ".duplicate").contains(",")) {
                        try {
                            String[] duplicateItems = cf.getString("item." + itemSlot + ".duplicate").split(",");
                            StringBuilder items = new StringBuilder();
                            for(int s = 0; s < duplicateItems.length; s++){
                                if(Integer.parseInt(e.getMessage()) != s+1) {
                                    items.append(duplicateItems[s]);
                                    items.append(",");
                                }
                            }
                            cf.set("item." + itemSlot + ".duplicate", items.toString());
                        } catch (Exception ex) {
                            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Не удалось удалить или найти элемент!"));
                            plugin.debug(ex,p);
                            break;
                        }
                        if(cf.getString("item." + itemSlot + ".duplicate").equals("")){
                            cf.set("item." + itemSlot + ".duplicate", null);
                        }
                    }else{
                        cf.set("item." + itemSlot + ".duplicate", null);
                    }
                }else{
                    p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Не найдено предметов, которые можно было бы удалить!"));
                    break;
                }
                saveGUIFile(cf, cfile, guiName, guiFile);
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Удален дубликат предмета: " + e.getMessage()));
                break;
        }
    }

    void itemSectionSectionCheck(Player p, String section, String guiName, ConfigurationSection cf, YamlConfiguration cfile, File guiFile, AsyncPlayerChatEvent e){
        String secondValue = section.split("\\.")[1];
        String itemSection = ChatColor.stripColor(section.replace("section." + secondValue + ".", ""));
        String playerMessage = ChatColor.stripColor(e.getMessage()).toLowerCase();
        switch (secondValue) {
            case "add":
                cf.set("item." + itemSection + "." + playerMessage + ".output", "true");
                if(playerMessage.equals("hasperm")) {
                    cf.set("item." + itemSection + "." + playerMessage + ".perm", "admin");
                }else{
                    cf.set("item." + itemSection + "." + playerMessage + ".value", "10");
                    cf.set("item." + itemSection + "." + playerMessage + ".compare", "%dm-player-balance%");
                }
                cf.set("item." + itemSection + "." + playerMessage + ".material", "DIRT");
                cf.set("item." + itemSection + "." + playerMessage + ".name", "");
                saveGUIFile(cf, cfile, guiName, guiFile);
                plugin.reloadGUIFiles();
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Добавлен раздел " + ChatColor.WHITE + playerMessage));
                break;
            case "remove":
                cf.set("item." + itemSection + "." + playerMessage, null);
                saveGUIFile(cf, cfile, guiName, guiFile);
                plugin.reloadGUIFiles();
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Удален раздел " + ChatColor.WHITE + playerMessage));
                break;
            case "change":
                cf.set("item." + itemSection + "." + playerMessage.split(":")[0], playerMessage.split(":")[1]);
                saveGUIFile(cf, cfile, guiName, guiFile);
                plugin.reloadGUIFiles();
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Установлено " + playerMessage.split(":")[0] + " на " + ChatColor.WHITE + playerMessage.split(":")[1]));
                break;
        }
    }
}
