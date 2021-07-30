package me.draimgoose.draimmenu;

import me.draimgoose.draimmenu.api.DraimMenuAPI;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.classresources.ExecuteOpenVoids;
import me.draimgoose.draimmenu.classresources.GetCustomHeads;
import me.draimgoose.draimmenu.classresources.ItemCreation;
import me.draimgoose.draimmenu.classresources.item_fall.ItemFallManager;
import me.draimgoose.draimmenu.classresources.placeholders.CreateText;
import me.draimgoose.draimmenu.classresources.placeholders.HexColours;
import me.draimgoose.draimmenu.classresources.placeholders.Placeholders;
import me.draimgoose.draimmenu.commands.*;
import me.draimgoose.draimmenu.commandtags.CommandTags;
import me.draimgoose.draimmenu.cutomcommands.CustomsCMD;
import me.draimgoose.draimmenu.datamanager.DebugManager;
import me.draimgoose.draimmenu.datamanager.GUIDataLoader;
import me.draimgoose.draimmenu.interactives.input.UserInputUtils;
import me.draimgoose.draimmenu.interactives.OpenOnJoin;
import me.draimgoose.draimmenu.ioclasses.Sequence_1_13;
import me.draimgoose.draimmenu.ioclasses.Sequence_1_14;
import me.draimgoose.draimmenu.ioclasses.nbt.NBTManager;
import me.draimgoose.draimmenu.ioclasses.legacy.LegacyVersion;
import me.draimgoose.draimmenu.ioclasses.legacy.MinecraftVersions;
import me.draimgoose.draimmenu.ioclasses.legacy.PlayerHeads;
import me.draimgoose.draimmenu.openguimanager.*;
import me.draimgoose.draimmenu.openwithitem.HotbarItemLoader;
import me.draimgoose.draimmenu.openwithitem.SwapItemEvent;
import me.draimgoose.draimmenu.openwithitem.UtilsChestSortEvent;
import me.draimgoose.draimmenu.openwithitem.UtilsOpenWithItem;
import me.draimgoose.draimmenu.guiblocks.BlocksTabComplete;
import me.draimgoose.draimmenu.guiblocks.DraimMenuBlocks;
import me.draimgoose.draimmenu.guiblocks.GUIBlockOnClick;
import me.draimgoose.draimmenu.playerinventoryhandler.InventorySaver;
import me.draimgoose.draimmenu.playerinventoryhandler.ItemStackSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.Callable;

public class DraimMenu extends JavaPlugin {
    public YamlConfiguration config;
    public Economy econ = null;
    public boolean openWithItem = false; //это будет верно, если есть панель с открытым элементом

    //ТЕГ
    public String tag = "&8(&cDMenu&8) &8> ";

    public List<Player> generateMode = new ArrayList<>(); //игроки, которые в данный момент находятся в режиме генерации
    public List<String[]> editorInputStrings = new ArrayList<>();
    public List<GUI> guiList = new ArrayList<>(); //содержит все панели, включенные в папку панели

    //получение альтернативных классов
    public CommandTags commandTags = new CommandTags(this);
    public GUIDataLoader guiData = new GUIDataLoader(this);
    public Placeholders placeholders = new Placeholders(this);
    public DebugManager debug = new DebugManager(this);
    public CreateText tex = new CreateText(this);
    public HexColours hex = new HexColours(this);

    public ExecuteOpenVoids openVoids = new ExecuteOpenVoids(this);
    public ItemCreation itemCreate = new ItemCreation(this);
    public GetCustomHeads customHeads = new GetCustomHeads(this);
    public PlayerHeads getHeads = new PlayerHeads(this);
    public LegacyVersion legacy = new LegacyVersion(this);

    public OpenGUILoader openGUIs = new OpenGUILoader(this);
    public OpenGUI createGUI = new OpenGUI(this);
    public GUIPermissions guiPerms = new GUIPermissions(this);
    public HotbarItemLoader hotbar = new HotbarItemLoader(this);
    public NBTManager nbt = new NBTManager(this);

    public InventorySaver inventorySaver = new InventorySaver(this);
    public ItemStackSerializer itemSerializer = new ItemStackSerializer(this);
    public UserInputUtils inputUtils = new UserInputUtils(this);

    public File guisf = new File(this.getDataFolder() + File.separator + "guis");
    public YamlConfiguration blockConfig; //где хранятся местоположения блоков панелей

    public void onEnable() {
        Bukkit.getLogger().info("DraimMenu v" + this.getDescription().getVersion() + " Загрузка плагина...");


        //Регистрация конфигов
        this.blockConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "blocks.yml"));
        guiData.dataConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "data.yml"));
        inventorySaver.inventoryConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "inventories.yml"));
        this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "config.yml"));

        //Сохранение config.yml
        File configFile = new File(this.getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            //создание нового конфига, если его нету
            try {
                FileConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("config.yml")));
                configFileConfiguration.save(configFile);
                this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "config.yml"));
            } catch (IOException var11) {
                Bukkit.getConsoleSender().sendMessage("(DMenu) >" + ChatColor.RED + "ПРЕДУПРЕЖДЕНИЕ: Не удалось сохранить файл конфигурации!");
            }
        }else{
            //проверка конфига на отсутствующие элементы
            try {
                YamlConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("config.yml")));
                this.config.addDefaults(configFileConfiguration);
                this.config.options().copyDefaults(true);
                this.config.save(new File(this.getDataFolder() + File.separator + "config.yml"));
            } catch (IOException var10) {
                Bukkit.getConsoleSender().sendMessage("(DMenu) >" + ChatColor.RED + " ПРЕДУПРЕЖДЕНИЕ: Не удалось сохранить файл конфигурации!");
            }
        }

        // установка класс файлов
        this.setupEconomy();
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        new Metrics(this);
        Objects.requireNonNull(this.getCommand("draimmenu")).setExecutor(new DraimMenu(this));
        Objects.requireNonNull(this.getCommand("draimmenureload")).setExecutor(new CommandGUIReload(this));
        Objects.requireNonNull(this.getCommand("draimmenudebug")).setExecutor(new CommandGUIDebug(this));
        Objects.requireNonNull(this.getCommand("draimmenuversion")).setExecutor(new CommandGUIVersion(this));
        Objects.requireNonNull(this.getCommand("draimmenulist")).setExecutor(new CommandGUIList(this));
        Objects.requireNonNull(this.getCommand("draimmenuimport")).setExecutor(new CommandGUIImport(this));
        this.getServer().getPluginManager().registerEvents(new Utils(this), this);
        this.getServer().getPluginManager().registerEvents(inventorySaver, this);
        this.getServer().getPluginManager().registerEvents(inputUtils, this);
        this.getServer().getPluginManager().registerEvents(new UtilsGUILoader(this), this);
        this.getServer().getPluginManager().registerEvents(new ItemFallManager(this), this);
        this.getServer().getPluginManager().registerEvents(new OpenOnJoin(this), this);

        //загрузка всех встроенных тегов
        commandTags.registerBuiltInTags();

        //если для кастом команд установлено значение false, не загружать их
        if(Objects.requireNonNull(config.getString("config.custom-commands")).equalsIgnoreCase("true")){
            this.getServer().getPluginManager().registerEvents(new CustomsCMD(this), this);
        }

        //если для элементов хотбара установлено значение false, не загружать его
        if(Objects.requireNonNull(config.getString("config.hotbar-items")).equalsIgnoreCase("true")){
            this.getServer().getPluginManager().registerEvents(new UtilsOpenWithItem(this), this);
        }


        //если для блоков установлено значение false, не загружайть их
        if(Objects.requireNonNull(config.getString("config.gui-blocks")).equalsIgnoreCase("true")){
            Objects.requireNonNull(this.getCommand("draimmenulock")).setExecutor(new DraimMenuBlocks(this));
            Objects.requireNonNull(this.getCommand("draimmenublock")).setTabCompleter(new BlocksTabComplete(this));
            this.getServer().getPluginManager().registerEvents(new GUIBlockOnClick(this), this);
        }

        //если MC 1.8, не использовать это
        if (!Bukkit.getVersion().contains("1.8")) {
            this.getServer().getPluginManager().registerEvents(new SwapItemEvent(this), this);
        }

        //если включен плуг ChestSort
        if(getServer().getPluginManager().isPluginEnabled("ChestSort")){
            this.getServer().getPluginManager().registerEvents(new UtilsChestSortEvent(this), this);
        }



        //Авто-обновочка
        reloadGUIFiles();

        //создание хотбар элементов
        hotbar.reloadHotbarSlots();

        //добавление кастомных диаграмм bStats
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SingleLineChart("gui_amount", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                //это общее количество загруженных панелей
                return guiList.size();
            }
        }));

        //тег
        tag = tex.colour(config.getString("config.format.tag") + " ");

        Bukkit.getLogger().info("DraimMenu v" + this.getDescription().getVersion() + " Плагин запушен!");
    }

    public void onDisable() {
        //Закрытие всех гуишек
        for(String name : openGUIs.openGUIs.keySet()){
            openGUIs.closeGUIForLoader(name, GUIPosition.Top);
            try {
                Bukkit.getPlayer(name).closeInventory();
            }catch (Exception ignore){}
        }

        //сохранение файлов
        guiData.saveDataFile();
        inventorySaver.saveInventoryFile();
        Bukkit.getLogger().info("DraimMenu успешно выключен.");
    }

    public static DraimMenuAPI getAPI(){
        return new DraimMenuAPI(JavaPlugin.getPlugin(DraimMenu.class));
    }

    public ItemStack setName(GUI gui, ItemStack renamed, String customName, List<String> lore, Player p, Boolean usePlaceholders, Boolean useColours, Boolean hideAttributes) {
        try {
            ItemMeta renamedMeta = renamed.getItemMeta();
            //установка плейсхолдера
            if(usePlaceholders){
                customName = tex.placeholdersNoColour(gui,GUIPosition.Top,p,customName);
            }
            if(useColours){
                customName = tex.colour(customName);
            }

            assert renamedMeta != null;
            //скрытие атрибутов, которые добовляют NBT тег
            if(hideAttributes) {
                renamedMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }
            if (customName != null) {
                renamedMeta.setDisplayName(customName);
            }

            List<String> re_lore;
            if (lore != null) {
                if(usePlaceholders && useColours){
                    re_lore = tex.placeholdersList(gui,GUIPosition.Top, p, lore, true);
                }else if(usePlaceholders){
                    re_lore = tex.placeholdersNoColour(gui,GUIPosition.Top,p, lore);
                }else if(useColours){
                    re_lore = tex.placeholdersList(gui,GUIPosition.Top, p, lore, false);
                }else{
                    re_lore = lore;
                }
                renamedMeta.setLore(splitListWithEscape(re_lore));
            }
            renamed.setItemMeta(renamedMeta);
        } catch (Exception ignored) {}
        return renamed;
    }

    private void setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
        } else {
            RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
            } else {
                this.econ = (Economy) rsp.getProvider();
            }
        }
    }

    public boolean checkGUIs(YamlConfiguration temp) {
        try {
            return temp.contains("guis");
        } catch (Exception var3) {
            return false;
        }
    }

    //проверка на одинаковые имена гуишек
    public boolean checkDuplicateGUI(CommandSender sender){
        ArrayList<String> agui = new ArrayList<>();
        for(GUI gui : guiList){
            agui.add(gui.getName());
        }

        //имена - это список названий гуишек
        Set<String> oset = new HashSet<String>(agui);
        if (oset.size() < agui.size()) {
            //если есть одинаковые имена гуишек
            ArrayList<String> oguiTemp = new ArrayList<String>();
            for(String tempName : agui){
                if(oguiTemp.contains(tempName)){
                    sender.sendMessage(tex.colour(tag) + ChatColor.RED + " Повторное название GUI-панели: " + tempName);
                    return false;
                }
                oguiTemp.add(tempName);
            }
            return false;
        }
        return true;
    }

    //просмотр всех файлов во всех папках
    public void fileNamesFromDirectory(File directory) {
        for (String fileName : Objects.requireNonNull(directory.list())) {
            if(new File(directory + File.separator + fileName).isDirectory()){
                fileNamesFromDirectory(new File(directory + File.separator + fileName));
                continue;
            }

            try {
                int ind = fileName.lastIndexOf(".");
                if (!fileName.substring(ind).equalsIgnoreCase(".yml") && !fileName.substring(ind).equalsIgnoreCase(".yaml")) {
                    continue;
                }
            }catch (Exception ex){
                continue;
            }

            //проверка перед добавлением файлов
            if(!checkGUIs(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)))){
                this.getServer().getConsoleSender().sendMessage("(DraimMenu)" + ChatColor.RED + " Ошибка в: " + fileName);
                continue;
            }
            for (String tempName : Objects.requireNonNull(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)).getConfigurationSection("guis")).getKeys(false)) {
                guiList.add(new GUI(new File((directory + File.separator + fileName)),tempName));
                if(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)).contains("gui." + tempName + ".open-with-item")) {
                    openWithItem = true;
                }
            }
        }
    }

    public void reloadGUIFiles() {
        guiList.clear();
        openWithItem = false;
        //загрузка гуишек
        fileNamesFromDirectory(guisf);
    }

    public void debug(Exception e, Player p) {
        if (p == null) {
            if(debug.consoleDebug){
                getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "(DraimMenu) Плагин выдал ошибку отладки, найдите ошибку ниже");
                e.printStackTrace();
            }
        }else{
            if(debug.isEnabled(p)){
                p.sendMessage(tag + ChatColor.DARK_RED + "Проверьте консоль на наличие подробной ошибки.");
                getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "(DraimMenu) Плагин выдал ошибку отладки, найдите ошибку ниже");
                e.printStackTrace();
            }
        }
    }

    public void helpMessage(CommandSender p) {
        p.sendMessage(tex.colour( tag + ChatColor.GREEN + "Команды:"));
        p.sendMessage(ChatColor.GOLD + "/dm <gui> [player:item] [player] " + ChatColor.WHITE + "Открыть командой GUI-панель.");
        if (p.hasPermission("draimmenu.reload")) {
            p.sendMessage(ChatColor.GOLD + "/dmr " + ChatColor.WHITE + "Перезагруть конфигурацию плагина..");
        }
        if (p.hasPermission("draimmenu.generate")) {
            p.sendMessage(ChatColor.GOLD + "/dmg <rows> " + ChatColor.WHITE + "Создать графический интерфейс.");
        }
        if (p.hasPermission("draimmenu.version")) {
            p.sendMessage(ChatColor.GOLD + "/dmv " + ChatColor.WHITE + "Отобразить текущую версию.");
        }
        if (p.hasPermission("draimmenu.update")) {
            p.sendMessage(ChatColor.GOLD + "/dmv latest " + ChatColor.WHITE + "Загрузить последнее обновление после перезагрузки/перезапуска сервера.");
            p.sendMessage(ChatColor.GOLD + "/dmv [version:cancel] " + ChatColor.WHITE + "Загрузить обновление после перезагрузки/перезапуска сервера.");
        }
        if (p.hasPermission("draimmenu.import")) {
            p.sendMessage(ChatColor.GOLD + "/dmi [file name] [URL] " + ChatColor.WHITE + "Импортировать необработанные текстовые панели.");
        }
        if (p.hasPermission("draimmenu.edit")) {
            p.sendMessage(ChatColor.GOLD + "/dme [gui] " + ChatColor.WHITE + "Редактирование панелей в игре.");
        }
        if (p.hasPermission("draimmenu.list")) {
            p.sendMessage(ChatColor.GOLD + "/dml " + ChatColor.WHITE + "Список загруженных меню.");
        }
        if (p.hasPermission("draimmenu.debug")) {
            p.sendMessage(ChatColor.GOLD + "/dmd " + ChatColor.WHITE + "Включение и отключение режима отладки.");
        }
        if (p.hasPermission("draimmenu.block.add")) {
            p.sendMessage(ChatColor.GOLD + "/dmb add <gui> " + ChatColor.WHITE + "Добавить GUI-панель на блок который вы смотрите.");
        }
        if (p.hasPermission("draimmenu.block.remove")) {
            p.sendMessage(ChatColor.GOLD + "/dmb remove " + ChatColor.WHITE + "Удалить GUI-панель с блока, на который вы смотрите.");
        }
        if (p.hasPermission("draimmenu.block.list")) {
            p.sendMessage(ChatColor.GOLD + "/dmb list " + ChatColor.WHITE + "Список блоков, на которых привязаны GUI-панели.");
        }
    }

    public final Map<String, Color> colourCodes = new HashMap<String, Color>() {{
        put("AQUA", Color.AQUA);
        put("BLUE", Color.BLUE);
        put("GRAY", Color.GRAY);
        put("GREEN", Color.GREEN);
        put("RED", Color.RED);
        put("WHITE", Color.WHITE);
        put("BLACK", Color.BLACK);
        put("FUCHSIA", Color.FUCHSIA);
        put("LIME", Color.LIME);
        put("MAROON", Color.MAROON);
        put("NAVY", Color.NAVY);
        put("OLIVE", Color.OLIVE);
        put("ORANGE", Color.ORANGE);
        put("PURPLE", Color.PURPLE);
        put("SILVER", Color.SILVER);
        put("TEAL", Color.TEAL);
        put("YELLOW", Color.YELLOW);
    }};

    public Reader getReaderFromStream(InputStream initialStream) throws IOException {
        //считывает зашифрованные файлы ресурсов в файле jar
        if(legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_13)){
            return new Sequence_1_13(this).getReaderFromStream(initialStream);
        }else{
            return new Sequence_1_14(this).getReaderFromStream(initialStream);
        }
    }

    //разделенее списков с использованием символа \n
    public List<String> splitListWithEscape(List<String> list){
        List<String> output = new ArrayList<>();
        for(String str : list){
            output.addAll(Arrays.asList(str.split("\\\\n")));
        }
        return output;
    }

    public int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("макс. значение должно быть выше минимального");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}