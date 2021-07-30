package me.draimgoose.draimmenu.ingameeditor;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.openguimanager.GUIOpenType;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class EditorUtils implements Listener {
    public YamlConfiguration tempEdit;
    public ArrayList<String> inventoryItemSettingsOpening = new ArrayList<>();
    DraimMenu plugin;
    public EditorUtils(DraimMenu pl) {
        this.plugin = pl;
        this.tempEdit = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "temp.yml"));
    }
    @EventHandler
    public void onClickMainEdit(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        //если инвентарь не является главным окном редактора
        try {
            if (Objects.requireNonNull(e.getClickedInventory()).getType() != InventoryType.CHEST) {
                return;
            }
        }catch(NullPointerException nu){return;}
        if(!p.getOpenInventory().getTitle().equals(ChatColor.stripColor(plugin.tex.colour("Редактор меню"))) || plugin.openGUIs.hasGUIOpen(p.getName(), GUIPosition.Top)){
            return;
        }
        if(e.getClickedInventory() != e.getView().getTopInventory()){
            return;
        }
        e.setCancelled(true);
        ArrayList<String> guiNames = new ArrayList<String>(); //все gui из ВСЕХ файлов (имена gui)
        ArrayList<String> guiTitles = new ArrayList<String>(); //все gui из ВСЕХ файлов (заголовки gui)
        ArrayList<ConfigurationSection> guiYaml = new ArrayList<ConfigurationSection>(); //все gui из ВСЕХ файлов (файлы yaml)
        try {
            for(GUI gui : plugin.guiList) { //будет перебирать все файлы в папке
                guiNames.add(plugin.tex.colour(gui.getName()));
                guiTitles.add(plugin.tex.colour( Objects.requireNonNull(gui.getConfig().getString("title"))));
                guiYaml.add(gui.getConfig());
            }
        }catch(Exception fail){
            //не удалось получить все имена gui (вероятно, gui не существует)
            plugin.debug(fail,p);
            return;
        }
        if(e.getSlot() == 48){
            //кнопка "предыдущая страница"
            try {
                if (Objects.requireNonNull(e.getCurrentItem()).getType() == Material.PAPER) {
                    plugin.editorGuis.openEditorGUI(p, -1);
                    p.updateInventory();
                    return;
                }
            }catch(NullPointerException ignored){}
        }
        if(e.getSlot() == 49){
            //индекс страницы sunflower
            if(Objects.requireNonNull(e.getCurrentItem()).getType() == Material.SLIME_BALL){
                p.updateInventory();
                return;
            }
        }
        if(e.getSlot() == 50){
            //кнопка "Следующая страница"
            try{
                if(Objects.requireNonNull(e.getCurrentItem()).getType() == Material.PAPER){
                    plugin.editorGuis.openEditorGUI(p, 1);
                    p.updateInventory();
                    return;
                }
            }catch(NullPointerException ignored){}
        }
        if(e.getSlot() == 45){
            //кнопка выхода
            p.closeInventory();
            p.updateInventory();
            return;
        }
        if(e.getSlot() <= 44){
            //если выбраны слоты для панелей
            try{
                if(Objects.requireNonNull(e.getCurrentItem()).getType() != Material.AIR){
                    if(e.getClick().isLeftClick() && !e.getClick().isShiftClick()){
                        //если щелкнуть левой кнопкой мыши
                        int count = 0;
                        for(String guiName : guiNames){
                            if(guiName.equals(ChatColor.stripColor(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName()))){
                                plugin.createGUI.openGui(new GUI(guiYaml.get(count), guiName), p,GUIPosition.Top,GUIOpenType.Editor,0);
                                return;
                            }
                            count +=1;
                        }
                    }else{
                        //если щелкнуть правой кнопкой мыши
                        int count = 0;
                        for(String guiName : guiNames){
                            if(guiName.equals(ChatColor.stripColor(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName()))){
                                plugin.editorGuis.openGUISettings(p,guiName,guiYaml.get(count));
                                return;
                            }
                            count +=1;
                        }
                        p.updateInventory();
                    }
                }
            }catch(NullPointerException ignored){}
        }
        p.updateInventory();
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        Player p = (Player)e.getWhoClicked();
        if(!p.getOpenInventory().getTitle().contains("Редактирования меню:") || plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Top)){
            return;
        }
        String guiName = ""; //все gui из ВСЕХ файлов (имена)
        File file = null; //все gui из ВСЕХ файлов (имена)
        YamlConfiguration config = new YamlConfiguration(); //все gui из ВСЕХ файлов (файлы yaml)
        boolean found = false;
        try {
            //новое в циклическом просмотре файлов для получения имен файлов
            for(GUI gui : plugin.guiList) { //будет перебирать все файлы в папке
                if (e.getView().getTitle().equals("Редактирования меню: " + gui.getName())) {
                    guiName = gui.getName();
                    file = gui.getFile();
                    config = YamlConfiguration.loadConfiguration(gui.getFile());
                    found = true;
                    break;
                }
            }
        }catch(Exception fail){
            //не удалось получить все имена (вероятно, gui не существует)
            plugin.debug(fail,p);
            return;
        }
        if(!found){
            return;
        }
        //это в основном просто определяет, перетаскивается ли что-то.
        try {
            if (tempEdit.contains("gui." + guiName + ".temp." + p.getName() + ".material")) {
                if (e.getOldCursor().getType() != Material.matchMaterial(Objects.requireNonNull(tempEdit.getString("gui." + guiName + ".temp." + p.getName() + ".material")))) {
                    clearTemp(p, guiName);
                    return;
                }
            }
        }catch(Exception ex){
            return;
        }
        //Я не могу использовать временную загрузку, потому что тип события другой, также мне нужно просмотреть все элементы
        if(tempEdit.contains("gui." + guiName + ".temp." + p.getName())){
            try {
                for (int slot : e.getInventorySlots()) {
                    config.set("gui." + guiName + ".item." + slot, tempEdit.get("gui." + guiName + ".temp." + p.getName()));
                    //стаки не могут быть сохранены в файл, потому что это не точно в случаях перетаскивания
                    if(config.contains("gui." + guiName + ".item." + slot + ".stack")){
                        config.set("gui." + guiName + ".item." + slot + ".stack",null);
                    }
                    saveFile(file, config);
                    saveFile(file, config);
                }
            }catch(NullPointerException nu){
                plugin.debug(nu,p);
            }
        }
    }
    @EventHandler
    public void onInventoryEdit(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        if(!p.getOpenInventory().getTitle().contains("Редактирование меню: ") || plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Top)){
            return;
        }
        String guiName = "";
        File file = null;
        YamlConfiguration config = new YamlConfiguration();
        boolean found = false;
        try {
            //новое в циклическом просмотре файлов для получения имен файлов
            for(GUI gui : plugin.guiList) { //будет перебирать все файлы в папке
                if (e.getView().getTitle().equals("Редактирование меню: " + gui.getName())) {
                    guiName = gui.getName();
                    file = gui.getFile();
                    config = YamlConfiguration.loadConfiguration(gui.getFile());
                    found = true;
                    break;
                }
            }
        }catch(Exception fail){
            //не удалось получить все имена (вероятно, gui не существует)
            plugin.debug(fail,p);
            return;
        }
        if(!found){
            return;
        }
        //изменение файл
        if(e.getClick().isShiftClick() && e.getClickedInventory() == e.getView().getTopInventory()){
            if(e.getInventory().getItem(e.getSlot()) == null) {
                return;
            }
            onEditGUIClose(p,e.getInventory(),e.getView());
            inventoryItemSettingsOpening.add(p.getName());
            //обновление конфигурации yaml
            config = YamlConfiguration.loadConfiguration(file);
            plugin.editorGuis.openItemSettings(p,guiName,config.getConfigurationSection("gui." + guiName + ".item." + e.getSlot()), String.valueOf(e.getSlot()));
            p.updateInventory();
            return;
        }
        if(tempEdit.contains("gui." + guiName + ".temp." + p.getName() + ".material")) {
            if(!plugin.getHeads.ifSkullOrHead(Objects.requireNonNull(e.getCursor()).getType().toString())) {
                //если материал не совпадает, а также не является PLAYER_HEAD
                if (e.getCursor().getType() != Material.matchMaterial(Objects.requireNonNull(tempEdit.getString("gui." + guiName + ".temp." + p.getName() + ".material")))) {
                    clearTemp(p, guiName);
                }
            }
        }
        if(e.getSlot() == -999){
            if(e.getCurrentItem() == null) {
                clearTemp(p, guiName);
            }
            return;
        }
        if(e.getAction() == InventoryAction.CLONE_STACK){
            saveTempItem(e, p, config, guiName);
            saveFile(file,config);
        }else if(e.getAction() == InventoryAction.PLACE_ALL){
            loadTempItem(e, p, config, file, guiName);
            clearTemp(p, guiName);
            saveFile(file,config);
        }else if(e.getAction() == InventoryAction.COLLECT_TO_CURSOR){
            saveTempItem(e, p, config, guiName);
            saveFile(file,config);
            removeOldItem(e, p, config, file, guiName);
        }else if(e.getAction() == InventoryAction.DROP_ALL_CURSOR){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.DROP_ALL_SLOT){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.DROP_ONE_CURSOR){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.DROP_ONE_SLOT){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.HOTBAR_SWAP){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.PLACE_SOME){
            loadTempItem(e, p, config, file, guiName);
            saveFile(file,config);
        }else if(e.getAction() == InventoryAction.SWAP_WITH_CURSOR){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.PICKUP_ALL){
            saveTempItem(e, p, config, guiName);
            saveFile(file,config);
            removeOldItem(e, p, config, file, guiName);
        }else if(e.getAction() == InventoryAction.PICKUP_HALF){
            saveTempItem(e, p, config, guiName);
            saveFile(file,config);
        }else if(e.getAction() == InventoryAction.PICKUP_ONE){
            saveTempItem(e, p, config, guiName);
            saveFile(file,config);
        }else if(e.getAction() == InventoryAction.PICKUP_SOME){
            saveTempItem(e, p, config, guiName);
            saveFile(file,config);
        }else if(e.getAction() == InventoryAction.PLACE_ONE){
            loadTempItem(e, p, config, file, guiName);
            saveFile(file,config);
        }
    }

    @EventHandler
    public void onPlayerCloseGUI(InventoryCloseEvent e){
        //это помещено здесь, чтобы избежать конфликтов, закрытие gui, если она открыта
        if(plugin.openGUIs.hasGUIOpen(e.getPlayer().getName(),GUIPosition.Top)){
            plugin.openGUIs.closeGUIForLoader(e.getPlayer().getName(),GUIPosition.Top);
            return;
        }
        //выполнение настроек редактора, если это не обычная менюшка
        if(inventoryItemSettingsOpening.contains(e.getPlayer().getName())) {
            inventoryItemSettingsOpening.remove(e.getPlayer().getName());
            return;
        }
        onEditGUIClose((Player) e.getPlayer(), e.getInventory(), e.getView());
    }

    @EventHandler
    public void onGUISettings(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        if(!p.getOpenInventory().getTitle().contains("Настроки меню:") || plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Top)){
            return;
        }
        e.setCancelled(true);
        String guiName = ""; //все gui из ВСЕХ файлов (имена)
        boolean found = false;
        boolean hotbarItems = false;
        try {
            //просмотр файлов, чтобы получить имена файлов
            for(GUI gui : plugin.guiList) { //будет перебирать все файлы в папке
                if(e.getView().getTitle().equals("Настроки меню: " + gui.getName())){
                    guiName = gui.getName();
                    if(gui.getConfig().contains("open-with-item")){
                        hotbarItems = true;
                    }
                    found = true;
                    break;
                }
            }
        }catch(Exception fail){
            //не удалось получить все имена (вероятно, gui не существует)
            plugin.debug(fail,p);
            return;
        }
        if(!found){
            return;
        }
        if(e.getSlot() == 1){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.perm"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новое разрешение"));
            p.closeInventory();
        }
        if(e.getSlot() == 3){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.title"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новое название"));
            p.closeInventory();
        }
        if(e.getSlot() == 5){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.sound-on-open"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новый id звука"));
            p.closeInventory();
        }
        if(e.getSlot() == 7){
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "gui.commands.add"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите Новую Команду"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "gui.commands.remove"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите команду для удаления (должно быть целое число)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 21){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.delete"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Ты уверен? (yes/no)"));
            p.closeInventory();
        }
        if(e.getSlot() == 23){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.rows"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите количество строк (от 1 до 6)"));
            p.closeInventory();
        }
        if(e.getSlot() == 13){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.empty"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новый id материала"));
            p.closeInventory();
        }
        if(e.getSlot() == 15){
            //добавляет возможность добавлять и удалять строки
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "gui.commands-on-open.add"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новую команду"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "gui.commands-on-open.remove"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите команду для удаления (должно быть целое число)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 17){
            //добавляет возможность добавлять и удалять типы
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "gui.guiType.add"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новый тип панели"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "gui.guiType.remove"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите тип панели для удаления (должно быть целое число)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 25){
            //добавляет возможность добавлять и удалять строки
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "gui.disabled-worlds.add"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новое название Мира"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "gui.disabled-worlds.remove"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите строку для удаления (должно быть целое число)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 11){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.name"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новое имя"));
            p.closeInventory();
        }
        if(e.getSlot() == 18){
            plugin.editorGuis.openEditorGUI(p,0);
            p.updateInventory();
        }
        if(e.getSlot() == 40){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.hotbar.material"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новый предмет"));
            p.closeInventory();
        }
        if(e.getSlot() == 38 && hotbarItems){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.hotbar.name"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новое имя"));
            p.closeInventory();
        }
        if(e.getSlot() == 36 && hotbarItems){
            //добавляет возможность добавлять и удалять строки
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.hotbar.lore.add"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новые описание к предмету"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.hotbar.lore.remove"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите строку описания для удаления (должно быть целое число)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 42 && hotbarItems){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.hotbar.stationary"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите расположение (от 0 до 35)"));
            p.closeInventory();
        }
        if(e.getSlot() == 44 && hotbarItems){
            //добавляет возможность добавлять и удалять строки
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.hotbar.commands.add"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новую команду предмета"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"gui.hotbar.commands.remove"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите командну для удаления (должно быть целое число)"));
            }
            p.closeInventory();
        }
    }

    @EventHandler
    public void onItemSettings(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        if(!p.getOpenInventory().getTitle().contains("Параметры предмета:") || plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Top)){
            return;
        }
        e.setCancelled(true);
        String guiName = ""; //все gui из ВСЕХ файлов (имена)
        ConfigurationSection guiYaml = null; //все gui из ВСЕХ файлов (имена)
        boolean found = false;
        try {
            //просмотр файлов, чтобы получить имена файлов
            for(GUI gui : plugin.guiList) { //будет перебирать все файлы в папке
                if(e.getView().getTitle().equals("Параметры предмета: " + gui.getName())){
                    guiName = gui.getName();
                    guiYaml = gui.getConfig();
                    found = true;
                    break;
                }
            }
        }catch(Exception fail){
            //не удалось получить все имена (вероятно, gui не существует)
            plugin.debug(fail,p);
            return;
        }
        if(!found){
            return;
        }
        String itemSlot;
        try {
            itemSlot = ChatColor.stripColor(e.getView().getTopInventory().getItem(35).getItemMeta().getDisplayName().split("\\s")[2]);
        }catch(Exception ex){
            plugin.getServer().getConsoleSender().sendMessage("(DraimMenu) Не удалось получить слот для предметов");
            plugin.debug(ex,p);
            return;
        }
        if(e.getSlot() == 1){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"item:" + itemSlot + ":name"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите имя нового предмета"));
            p.closeInventory();
        }
        if(e.getSlot() == 3){
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "item:" + itemSlot + ":commands:add"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новую команду предмета"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "item:" + itemSlot + ":commands:remove"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите команду для удаления (должно быть целое число)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 5){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"item:" + itemSlot + ":enchanted"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите зачарования для предмета"));
            p.closeInventory();
        }
        if(e.getSlot() == 7){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"item:" + itemSlot + ":potion"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите эффект зелья для предмета"));
            p.closeInventory();
        }
        if(e.getSlot() == 13){
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "item:" + itemSlot + ":duplicate:add"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите распложение дубликата предмета"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "item:" + itemSlot + ":duplicate:remove"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите расположение дубликата для удаления (должно быть целое число)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 19){
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "item:" + itemSlot + ":lore:add"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите описание для предмета"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), guiName, "item:" + itemSlot + ":lore:remove"});
                p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите линию описания для удалния (должно быть целое число)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 21){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"item:" + itemSlot + ":stack"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите кол-во предметов ((должно быть целое число)"));
            p.closeInventory();
        }
        if(e.getSlot() == 23){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"item:" + itemSlot + ":customdata"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите кастом предмет"));
            p.closeInventory();
        }
        if(e.getSlot() == 25){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"item:" + itemSlot + ":leatherarmor"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новый цвет кожаной брони"));
            p.closeInventory();
        }
        if(e.getSlot() == 31){
            plugin.editorGuis.openItemSections(p,guiName,guiYaml.getConfigurationSection("item." + itemSlot), itemSlot);
            p.updateInventory();
        }
        if(e.getSlot() == 35){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"item:" + itemSlot + ":head"});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите новый кастом предмет (например, dms= self)"));
            p.closeInventory();
        }
        if(e.getSlot() == 27){
            if(itemSlot.contains(".")){
                String newSection = itemSlot.substring(0, itemSlot.lastIndexOf("."));
                plugin.editorGuis.openItemSections(p,guiName,guiYaml.getConfigurationSection("item." + newSection), newSection);
            }else {
                plugin.createGUI.openGui(new GUI(guiYaml, guiName), p,GUIPosition.Top, GUIOpenType.Editor, 0);
            }
            p.updateInventory();
        }
    }

    @EventHandler
    public void onItemSection(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        if(!p.getOpenInventory().getTitle().contains("Разделы предметов:") || plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Top)){
            return;
        }
        e.setCancelled(true);
        String guiName = "";
        ConfigurationSection guiYaml = null;
        ConfigurationSection itemConfSection;
        boolean found = false;
        try {
            for(GUI gui : plugin.guiList) {
                if(e.getView().getTitle().equals("Разделы предметов: " + gui.getName())){
                    guiName = gui.getName();
                    guiYaml = gui.getConfig();
                    found = true;
                    break;
                }
            }
        }catch(Exception fail){
            plugin.debug(fail,p);
            return;
        }
        if(!found){
            return;
        }

        String section;
        try {
            section = ChatColor.stripColor(Objects.requireNonNull(Objects.requireNonNull(e.getView().getTopInventory().getItem(44)).getItemMeta()).getDisplayName().split("\\s")[2]);
        }catch(Exception ex){
            plugin.getServer().getConsoleSender().sendMessage("(DraimMenu) Не удалось получить слот для предметов");
            plugin.debug(ex,p);
            return;
        }
        itemConfSection = guiYaml.getConfigurationSection("item." + section);

        if(e.getSlot() <= 35){
            if(e.getInventory().getItem(e.getSlot()) != null){
                if(e.getClick().isLeftClick()) {
                    String newSection = section + "." + ChatColor.stripColor(e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName());
                    plugin.editorGuis.openItemSettings(p, guiName, itemConfSection.getConfigurationSection(ChatColor.stripColor(e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName())), newSection);
                    p.updateInventory();
                }else{
                    String itemNameSection = "." + ChatColor.stripColor(e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName());
                    plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"section.change." + section + itemNameSection});
                    p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите настройки для изменения, например, value:500"));
                    p.closeInventory();
                }
            }
        }

        if(e.getSlot() == 38){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"section.remove." + section});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите название раздела для удаления, например, hasperm или hasperm0"));
            p.closeInventory();
        }

        if(e.getSlot() == 42){
            plugin.editorInputStrings.add(new String[]{p.getName(),guiName,"section.add." + section});
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.WHITE + "Введите название раздела для добавления, например, hasperm или hasperm0"));
            p.closeInventory();
        }

        if(e.getSlot() == 36){
            plugin.editorGuis.openItemSettings(p,guiName,itemConfSection, section);
            p.updateInventory();
        }
    }

    public void saveTempItem(InventoryClickEvent e, Player p, YamlConfiguration file, String guiName){
        tempEdit.set("gui." + guiName + ".temp." + p.getName(),file.get("gui." + guiName + ".item." + e.getSlot()));
        saveFile("temp.yml", tempEdit);
    }
        if(tempEdit.contains("gui." + guiName + ".temp." + p.getName())){
            config.set("gui." + guiName + ".item." + e.getSlot(),tempEdit.get("gui." + guiName + ".temp." + p.getName()));
            saveFile(file, config);
        }
    }
    public void removeOldItem(InventoryClickEvent e, Player p, YamlConfiguration config,File file, String guiName){
        config.set("gui." + guiName + ".item." + e.getSlot(),null);
        saveFile(file, config);
    }
    public void clearTemp(Player p, String guiName){
        tempEdit.set("gui." + guiName + ".temp." + p.getName(),null);
        saveFile("temp.yml", tempEdit);
    }
    public void saveFile(String fileName, YamlConfiguration file){
        try {
            file.save(new File(plugin.getDataFolder() + File.separator + fileName));
        } catch (IOException s) {
            plugin.debug(s,null);
        }
    }
    public void saveFile(File file, YamlConfiguration config){
        try {
            config.save(file);
        } catch (IOException s) {
            plugin.debug(s,null);
        }
    }

    @SuppressWarnings("deprecation")
    public void onEditGUIClose(Player p, Inventory inv, InventoryView invView) {
        if(!p.getOpenInventory().getTitle().contains("Редактирование меню:")){
            return;
        }
        String guiName = "";
        File file = null;
        YamlConfiguration config = new YamlConfiguration();
        boolean found = false;
        try {
            for(GUI gui : plugin.guiList) {
                if (invView.getTitle().equals("Редактирование меню: " + gui.getName())) {
                    guiName = gui.getName();
                    file = gui.getFile();
                    config = YamlConfiguration.loadConfiguration(gui.getFile());
                    found = true;
                    break;
                }
            }
        }catch(Exception fail){
            plugin.debug(fail,p);
            return;
        }
        if(!found){
            return;
        }
        config = plugin.itemCreate.generateGUIFile(guiName,inv,config);
        try {
            config.save(file);
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Сохранения изменены!"));
        } catch (IOException s) {
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Не удалось сохранить изменения!"));
            plugin.debug(s,p);
        }
        plugin.reloadGUIFiles();
    }
}
