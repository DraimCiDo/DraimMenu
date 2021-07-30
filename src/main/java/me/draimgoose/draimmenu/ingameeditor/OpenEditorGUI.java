package me.draimgoose.draimmenu.ingameeditor;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.ioclasses.legacy.MinecraftVersions;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OpenEditorGUI {
    DraimMenu plugin;
    public OpenEditorGUI(DraimMenu pl) {
        plugin = pl;
    }

    public void openEditorGUI(Player p, int pageChange) {
        Inventory i = Bukkit.createInventory(null, 54, "Редактор Меню");
        ArrayList<String> guiNames = new ArrayList<>(); //все панели из ВСЕХ файлов (имена панелей)
        ArrayList<String> guiTitles = new ArrayList<>(); //все панели из ВСЕХ файлов (заголовки панелей)
        ArrayList<ItemStack> guiItems = new ArrayList<>(); //все панели из ВСЕХ файлов (материалы панелей)
        try {
            for(GUI gui : plugin.guiList) { //будет перебирать все файлы в папке
                guiNames.add(plugin.tex.colour(gui.getName()));
                guiTitles.add(plugin.tex.colour(gui.getConfig().getString("title")));
                if (gui.getConfig().contains("open-with-item.material")) {
                    guiItems.add(gui.getHotbarItem(p));
                } else {
                    guiItems.add(new ItemStack(Material.PAPER));
                }
            }
        } catch (Exception fail) {
            //не удалось получить все имена панелей (вероятно, панелей не существует)
            plugin.debug(fail,p);
            return;
        }

        int pageNumber = 1;
        if (p.getOpenInventory().getTitle().equals("Редактор Меню")) {
            pageNumber = Integer.parseInt(ChatColor.stripColor(Objects.requireNonNull(Objects.requireNonNull(p.getOpenInventory().getItem(49)).getItemMeta()).getDisplayName()).replace("Page ", ""));
        }
        //добавит разницу
        pageNumber = pageNumber + pageChange;
        if (pageNumber <= 0) {
            //дважды проверяет номер страницы НЕ ниже 1
            pageNumber = 1;
        }
        //получит общее количество страниц
        int pagesAmount = (int) Math.ceil(guiNames.size() / 45.0);
        //сделает все элементы нижней панели
        ItemStack temp;
        temp = new ItemStack(Material.SLIME_BALL, 1);
        plugin.setName(null,temp, ChatColor.WHITE + "Страница " + pageNumber, null, p, true, true, true);
        i.setItem(49, temp);
        temp = new ItemStack(Material.BARRIER, 1);
        plugin.setName(null,temp, ChatColor.RED + "Выйти из меню", null, p, true, true, true);
        i.setItem(45, temp);
        temp = new ItemStack(Material.BOOK, 1);
        List<String> lore = new ArrayList();
        lore.add(ChatColor.GRAY + "- Нажмите, чтобы отредактировать предметы");
        lore.add(ChatColor.GRAY + "- ПКМ, чтобы изменить настройки.");
        lore.add(ChatColor.GRAY + "- ЛКМ по предмету, чтобы отредактировать предметы в меню");
        lore.add(ChatColor.GRAY + "- При вводе значения,");
        lore.add(ChatColor.GRAY + "  пропишите 'remove' чтобы установить");
        lore.add(ChatColor.GRAY + "  значение по умолчанию и используйте");
        lore.add(ChatColor.GRAY + "  '" + plugin.config.getString("config.input-cancel") + "' для отмены.");
        plugin.setName(null,temp, ChatColor.WHITE + "Советы по редактированию", lore, p, true, true, true);
        i.setItem(53, temp);
        if (pageNumber != 1) {
            //кнопка "Показать предыдущую страницу" отображается только в том случае, если номер не один
            temp = new ItemStack(Material.PAPER, 1);
            plugin.setName(null,temp, ChatColor.WHITE + "Предыдущая Страница", null, p, true, true, true);
            i.setItem(48, temp);
        }
        if (pageNumber < pagesAmount) {
            //если номер страницы указан в разделе количество страниц
            temp = new ItemStack(Material.PAPER, 1);
            plugin.setName(null,temp, ChatColor.WHITE + "Следующая страница", null, p, true, true, true);
            i.setItem(50, temp);
        }
        int count = 0;
        int slot = 0;
        lore.clear();
        for (String guiName : guiNames) {
            //количество равно +1, потому что количество начинается с 0, а не с 1
            if ((pageNumber * 45 - 45) < (count + 1) && (pageNumber * 45) > (count)) {
                temp = guiItems.get(count);
                plugin.setName(null,temp, ChatColor.WHITE + guiName, lore, p, false, true, true);
                i.setItem(slot, temp);
                slot += 1;
            }
            count += 1;
        }
        p.openInventory(i);
    }

    @SuppressWarnings("deprecation")
    public void openGUISettings(Player p, String panelName, ConfigurationSection cf) {
        Inventory i = Bukkit.createInventory(null, 45, ChatColor.stripColor("Настройки меню: " + panelName));
        List<String> lore = new ArrayList();
        ItemStack temp;
        //удаляет, если у игрока уже была строка из предыдущего
        for (int o = 0; plugin.editorInputStrings.size() > o; o++) {
            if (plugin.editorInputStrings.get(o)[0].equals(p.getName())) {
                plugin.editorInputStrings.remove(o);
                o = o - 1;
            }
        }
        //создаёт все предметы
        temp = new ItemStack(Material.IRON_INGOT, 1);
        lore.add(ChatColor.GRAY + "Разрешение, необходимое для открытия панели");
        lore.add(ChatColor.GRAY + "draimmenu.panel.[insert]");
        if (cf.contains("perm")) {
            lore.add(ChatColor.WHITE + "--------------------------------");
            lore.add(ChatColor.WHITE + "draimmenu.panel." + cf.getString("perm"));
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Разрешение меню", lore, p, true, true, true);
        i.setItem(1, temp);

        temp = new ItemStack(Material.NAME_TAG, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Вверхняя строчка меню");
        if (cf.contains("title")) {
            lore.add(ChatColor.WHITE + "------------------");
            lore.add(ChatColor.WHITE + cf.getString("title"));
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Название меню", lore, p, true, true, true);
        i.setItem(3, temp);

        temp = new ItemStack(Material.JUKEBOX, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Звук при открытии меню");
        if (cf.contains("sound-on-open")) {
            lore.add(ChatColor.WHITE + "------------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("sound-on-open")).toUpperCase());
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Звук меню", lore, p, true, true, true);
        i.setItem(5, temp);

        temp = new ItemStack(Material.IRON_DOOR, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Кастомные команды для открытия меню");
        lore.add(ChatColor.GRAY + "- ЛКМ, чтобы добавить команду");
        lore.add(ChatColor.GRAY + "- ПКМ, чтобы удалить команду");
        if (cf.contains("commands")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("commands")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Команды меню", lore, p, true, true, true);
        i.setItem(7, temp);

        temp = new ItemStack(Material.LAVA_BUCKET, 1);
        lore.clear();
        lore.add(ChatColor.DARK_RED + "Удалить принудительно меню");
        plugin.setName(null,temp, ChatColor.RED + "Удалить меню", lore, p, true, true, true);
        i.setItem(21, temp);

        temp = new ItemStack(Material.LADDER, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Сколько строк будет в меню,");
        lore.add(ChatColor.GRAY + "выберите целое число от 1 до 6");
        plugin.setName(null,temp, ChatColor.WHITE + "Размер меню", lore, p, true, true, true);
        i.setItem(23, temp);

        temp = new ItemStack(Material.STONE, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Миры, которые не могут получить доступ к меню");
        lore.add(ChatColor.GRAY + "- ЛКМ, чтобы добавить мир");
        lore.add(ChatColor.GRAY + "- ПКМ, чтобы удалить мир");
        if (cf.contains("disabled-worlds")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("disabled-worlds")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Отключение миров", lore, p, true, true, true);
        i.setItem(25, temp);

        temp = new ItemStack(Material.GLASS, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Заполнить пустые слоты предметом");
        if (cf.contains("empty")) {
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("empty")).toUpperCase());
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Пустые слоты меню", lore, p, true, true, true);
        i.setItem(13, temp);

        temp = new ItemStack(Material.ANVIL, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Выполнение команд при открытии");
        lore.add(ChatColor.GRAY + "- ЛКМ, чтобы добавить команду");
        lore.add(ChatColor.GRAY + "- ПКМ, чтобы удалить команду");
        if (cf.contains("commands-on-open")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("commands-on-open")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Команда меню", lore, p, true, true, true);
        i.setItem(15, temp);

        temp = new ItemStack(Material.STRING, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Специальные типы панелей");
        lore.add(ChatColor.GRAY + "- ЛКМ, чтобы добавить тип меню");
        lore.add(ChatColor.GRAY + "- ПКМ, чтобы убрать тип меню");
        if (cf.contains("panelType")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("panelType")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Тип меню", lore, p, true, true, true);
        i.setItem(17, temp);

        temp = new ItemStack(Material.ITEM_FRAME, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Кодовое имя для открытия меню");
        lore.add(ChatColor.GRAY + "/dm [name]");
        lore.add(ChatColor.WHITE + "-----------------------");
        lore.add(ChatColor.WHITE + panelName);
        plugin.setName(null,temp, ChatColor.WHITE + "Название меню", lore, p, true, true, true);
        i.setItem(11, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        plugin.setName(null,temp, ChatColor.RED + "Назад", null, p, true, true, true);
        i.setItem(18, temp);

        //Это создаст стену из стеклянных панелей, разделяющую настройки панели с настройками горячей панели
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)) {
            temp = new ItemStack(Material.matchMaterial("STAINED_GLASS_PANE"), 1,(short)15);
        }else{
            temp = new ItemStack(Material.matchMaterial("BLACK_STAINED_GLASS_PANE"), 1);
        }
        plugin.setName(null,temp, ChatColor.WHITE + "", null, p,false, true, true);
        for(int d = 27; d < 36; d++){
            i.setItem(d, temp);
        }
        //Это предметы для хотбара (открытие с помощью предмета)
        boolean hotbarItems = false;

        if(cf.contains("open-with-item.material")){
            hotbarItems = true;
            temp = plugin.itemCreate.makeItemFromConfig(null, GUIPosition.Top,cf.getConfigurationSection("open-with-item"), p, false, true, false);
        }else{
            temp = new ItemStack(Material.REDSTONE_BLOCK, 1);
        }
        lore.clear();
        lore.add(ChatColor.GRAY + "Текущий предмет");
        if (cf.contains("open-with-item.material")) {
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("open-with-item.material")).toUpperCase());
        }else{
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.RED + "ОТКЛЮЧЕН");
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Откртые по предмету", lore, p, true, true, true);
        i.setItem(40, temp);

        if(hotbarItems) {
            temp = new ItemStack(Material.NAME_TAG, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Название для хотбар предмета");
            if (cf.contains("open-with-item.name")) {
                lore.add(ChatColor.WHITE + "----------");
                lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("open-with-item.name")));
            }
            plugin.setName(null,temp, ChatColor.WHITE + "Название хотбар предмета", lore, p, true, true, true);
            i.setItem(38, temp);

            temp = new ItemStack(Material.FEATHER, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Отображемое описание на хотбар предмете");
            lore.add(ChatColor.GRAY + "- ЛКМ, чтобы добавить описание");
            lore.add(ChatColor.GRAY + "- ПКМ, чтобы убрать описание");
            if (cf.contains("open-with-item.lore")) {
                lore.add(ChatColor.WHITE + "-------------------------------");
                int count = 1;
                for (String tempLore : cf.getStringList("open-with-item.lore")) {
                    lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                    count += 1;
                }
            }
            plugin.setName(null,temp, ChatColor.WHITE + "Описание хотбар предмета", lore, p, true, true, true);
            i.setItem(36, temp);

            temp = new ItemStack(Material.BEDROCK, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Расположение хотбар предмета");
            lore.add(ChatColor.GRAY + "выберите число от 0 до 35");
            if (cf.contains("open-with-item.stationary")) {
                lore.add(ChatColor.WHITE + "-------------------------");
                int location = cf.getInt("open-with-item.stationary");
                lore.add(ChatColor.WHITE + String.valueOf(location));
            }
            plugin.setName(null,temp, ChatColor.WHITE + "Расположение хотбар предмета", lore, p, true, true, true);
            i.setItem(42, temp);

            temp = new ItemStack(Material.BOOK, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Команды для хотбар предмета");
            lore.add(ChatColor.GRAY + "- ЛКМ, чтобы добавить команду");
            lore.add(ChatColor.GRAY + "- ПКМ, чтобы убрать команду");
            lore.add(ChatColor.GRAY + "Если будут добавлены команды, предмет");
            lore.add(ChatColor.GRAY + "не будет автоматически открывать панель");
            if (cf.contains("open-with-item.commands")) {
                lore.add(ChatColor.WHITE + "-------------------------------");
                int count = 1;
                for (String tempLore : cf.getStringList("open-with-item.commands")) {
                    lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                    count += 1;
                }
            }
            plugin.setName(null,temp, ChatColor.WHITE + "Команды для хотбар предмета", lore, p, true, true,true);
            i.setItem(44, temp);
        }

        p.openInventory(i);
    }

    //раздел похож на раздел hassection, но с прорезью, например, 1.hasperm.hasvalue
    public void openItemSettings(Player p, String panelName, ConfigurationSection cf, String section) {
        Inventory i = Bukkit.createInventory(null, 36, ChatColor.stripColor("Настройки предметов: " + panelName));
        List<String> lore = new ArrayList();
        ItemStack temp;
        //удаляет, если у игрока уже была строка из предыдущего
        for (int o = 0; plugin.editorInputStrings.size() > o; o++) {
            if (plugin.editorInputStrings.get(o)[0].equals(p.getName())) {
                plugin.editorInputStrings.remove(o);
                o = o - 1;
            }
        }
        //make all the items
        temp = new ItemStack(Material.NAME_TAG, 1);
        lore.add(ChatColor.GRAY + "Отображаемое имя предмета в меню");
        if (cf.contains("name")) {
            if (!Objects.equals(cf.getString("name"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("name"));
            }
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Название предмета", lore, p, true, true, true);
        i.setItem(1, temp);

        temp = new ItemStack(Material.ANVIL, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Команда по нажатию");
        lore.add(ChatColor.GRAY + "- ЛКМ, чтобы добавить команду");
        lore.add(ChatColor.GRAY + "- ПКМ, чтобы убрать команду");
        if (cf.contains("commands")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("commands")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Команды предмета", lore, p, true, true, true);
        i.setItem(3, temp);

        temp = new ItemStack(Material.ENCHANTED_BOOK, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Отображемое очарование предмета в меню");
        if (cf.contains("enchanted")) {
            if (!Objects.equals(cf.getString("name"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("enchanted"));
            }
        } else {
            lore.add(ChatColor.WHITE + "--------------------------------");
            lore.add(ChatColor.WHITE + "false");
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Зачарования предмета", lore, p, true, true, true);
        i.setItem(5, temp);

        temp = new ItemStack(Material.POTION, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Отображеный эффект зелья предмета в меню");
        if (cf.contains("potion")) {
            if (!Objects.equals(cf.getString("potion"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("potion"));
            }
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Эффект зелья на предмете", lore, p, true, true, true);
        i.setItem(7, temp);

        temp = new ItemStack(Material.PAPER, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Создание дубликата предмета в других слотах");
        lore.add(ChatColor.GRAY + "- ЛКМ, чтобы создать дубликат");
        lore.add(ChatColor.GRAY + "- ПКМ, чтобы удалить дубликат");
        if (cf.contains("duplicate")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getString("duplicate").split(",")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Дубликат предмета", lore, p, true, true, true);
        i.setItem(13, temp);

        temp = new ItemStack(Material.FEATHER, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Отобразить лор под названием предмета");
        lore.add(ChatColor.GRAY + "- ЛКМ, чтобы добавить линию лора");
        lore.add(ChatColor.GRAY + "- ПКМ, чтобы удалить линию лора");
        if (cf.contains("lore")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("lore")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Лор предмета", lore, p, true, true, true);
        i.setItem(19, temp);

        temp = new ItemStack(Material.ITEM_FRAME, 2);
        lore.clear();
        lore.add(ChatColor.GRAY + "Кол-во предмета(просто отображение цифр)");
        if (cf.contains("stack")) {
            if (!Objects.equals(cf.getString("stack"), "")) {
                try {
                    temp.setAmount(Integer.parseInt(Objects.requireNonNull(cf.getString("stack"))));
                } catch (Exception ignored) {
                }
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("stack"));
            }
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Размер стака предмета", lore, p, true, true, true);
        i.setItem(21, temp);

        if(!plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)) {
            temp = new ItemStack(Material.PAINTING, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Добавление Cutsom Model Data");
            if (cf.contains("customdata")) {
                if (!Objects.equals(cf.getString("customdata"), "")) {
                    lore.add(ChatColor.WHITE + "--------------------------------");
                    lore.add(ChatColor.WHITE + cf.getString("customdata"));
                }
            }
            plugin.setName(null,temp, ChatColor.WHITE + "Custom Model Data", lore, p, true, true, true);
            i.setItem(23, temp);
        }

        temp = new ItemStack(Material.COMPASS, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Просмотр предмета по-разному");
        lore.add(ChatColor.GRAY + "Разделы и добавление сложных значений.");
        lore.add(ChatColor.WHITE + "--------------------------------");
        lore.add(ChatColor.WHITE + section);
        plugin.setName(null,temp, ChatColor.WHITE + "Разделы предмета", lore, p, true, true, true);
        i.setItem(31, temp);

        temp = new ItemStack(Material.LEATHER_HELMET, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Выберите цвет для брони");
        lore.add(ChatColor.GRAY + "используйте r,g,b или цвет API spigot`а");
        if (cf.contains("leatherarmor")) {
            if (!Objects.equals(cf.getString("leatherarmor"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("leatherarmor"));
            }
        }
        plugin.setName(null,temp, ChatColor.WHITE + "Цвет кожаной брони", lore, p, true, true, true);
        i.setItem(25, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        plugin.setName(null,temp, ChatColor.RED + "Back", null, p, true, true, true);
        i.setItem(27, temp);

        temp = plugin.itemCreate.makeItemFromConfig(null,GUIPosition.Top,cf,p,false,false, false);
        lore.clear();
        lore.add(ChatColor.GRAY + "Нажмите, чтобы задать кастом предмет,");
        lore.add(ChatColor.GRAY + "как правило, для пользовательских головок");
        plugin.setName(null,temp, ChatColor.WHITE + "Раздел предмета " + section + " Предварительный просмотр", lore, p, true, true, true);
        i.setItem(35, temp);

        p.openInventory(i);
    }

    //раздел похож на раздел hassection, но с прорезью, например, 1.hasperm.hasvalue
    public void openItemSections(Player p, String panelName, ConfigurationSection cf, String itemSection) {
        Inventory i = Bukkit.createInventory(null, 45, ChatColor.stripColor("Раздел предметов: " + panelName));
        ItemStack temp;
        int slot = 0;
        for(String section : cf.getKeys(false)){
            //получить список разделов предметов
            if(slot > 35){
                break;
            }
            if(section.contains("hasperm") || section.contains("hasvalue") || section.contains("hasgreater")){
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "ЛКМ, чтобы открыть предмет");
                lore.add(ChatColor.GRAY + "ПКМ, чтобы изменить настройки");
                if(cf.contains(section + ".output")) {
                    lore.add(ChatColor.WHITE + "Отпут: " + ChatColor.GRAY + cf.getString(section + ".output"));
                }else{
                    lore.add(ChatColor.WHITE + "Отпут: " + ChatColor.GRAY + "true");
                }
                if(cf.contains(section + ".perm")) {
                    lore.add(ChatColor.WHITE + "Права: " + ChatColor.GRAY + cf.getString(section + ".perm"));
                }
                if(cf.contains(section + ".value")) {
                    lore.add(ChatColor.WHITE + "Значение: " + ChatColor.GRAY + cf.getString(section + ".value"));
                }
                if(cf.contains(section + ".compare")) {
                    lore.add(ChatColor.WHITE + "Сравнение: " + ChatColor.GRAY + cf.getString(section + ".compare"));
                }

                temp = plugin.itemCreate.makeItemFromConfig(null,GUIPosition.Top,cf.getConfigurationSection(section),p,false,false, false);
                plugin.setName(null,temp, ChatColor.AQUA + section, lore, p,false, true, true);
                i.setItem(slot, temp);
                slot++;
            }
        }

        temp = new ItemStack(Material.REDSTONE, 1);
        plugin.setName(null,temp, ChatColor.WHITE + "Удалить раздел", null, p, true, true, true);
        i.setItem(38, temp);

        temp = new ItemStack(Material.SLIME_BALL, 1);
        plugin.setName(null,temp, ChatColor.WHITE + "Добавить раздел", null, p, true, true, true);
        i.setItem(42, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        plugin.setName(null,temp, ChatColor.RED + "Назад", null, p, true, true, true);
        i.setItem(36, temp);

        temp = new ItemStack(Material.BOOK, 1);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Тип разделов:");
        lore.add(ChatColor.GRAY + "- hasperm");
        lore.add(ChatColor.GRAY + "- hasvalue");
        lore.add(ChatColor.GRAY + "- hasgreater");
        plugin.setName(null,temp, ChatColor.WHITE + "Раздел предмета " + itemSection, lore, p, true, true, true);
        i.setItem(44, temp);

        p.openInventory(i);
    }
}
