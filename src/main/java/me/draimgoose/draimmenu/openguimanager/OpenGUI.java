package me.draimgoose.draimmenu.openguimanager;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class OpenGUI {
    DraimMenu plugin;
    public OpenGUI(DraimMenu pl) {
        this.plugin = pl;
    }

    @SuppressWarnings("deprecation")
    public Inventory openGui(GUI gui, Player p,GUIPosition position, GUIOpenType openType, int animateValue) {
        ConfigurationSection pconfig = gui.getConfig();

        Inventory i;
        if(position == GUIPosition.Top) {
            String title;
            if (openType !=GUIOpenType.Editor) {
                //обычный инв
                title = plugin.tex.placeholders(gui, position, p, pconfig.getString("title"));
            } else {
                //эдитор
                title = "Редактирование меню: " + gui.getName();
            }

            if (isNumeric(pconfig.getString("rows"))) {
                i = Bukkit.createInventory(p, pconfig.getInt("rows") * 9, title);
            } else {
                i = Bukkit.createInventory(p, InventoryType.valueOf(pconfig.getString("rows")), title);
            }
        }else{
            i = p.getInventory();
            //если middle или bottom позиция, старые предметы очищаются
            for (int c = 0; getInvSize(i,position) > c; ++c) {
                if(pconfig.getConfigurationSection("item").getKeys(false).contains(String.valueOf(c))){
                    continue;
                }
                setItem(null, c, i, p, position);
            }
        }

        Set<String> itemList = pconfig.getConfigurationSection("item").getKeys(false);
        HashSet<Integer> takenSlots = new HashSet<>();
        for (String item : itemList) {
            String section = "";
            //openType должен быть не 3, чтобы редактор не включал элементы hasperm и hasvalue и т.л.
            if (openType != GUIOpenType.Editor) {
                section = plugin.itemCreate.hasSection(gui,position,pconfig.getConfigurationSection("item." + Integer.parseInt(item)), p);
                //Этот раздел предназначен для анимации: VISUAL ONLY

                //проверяет, есть ли анимация в разделе "Элементы"
                if (pconfig.contains("item." + item + section + ".animate" + animateValue)) {
                    //проверяет, содержит ли он анимацию значение animvatevalue
                    if (pconfig.contains("item." + item + section + ".animate" + animateValue)) {
                        section = section + ".animate" + animateValue;
                    }
                }
            }

            //добавит NBT только в том случае, если не будет графического интерфейса редактора
            ItemStack s = plugin.itemCreate.makeItemFromConfig(gui,position,Objects.requireNonNull(pconfig.getConfigurationSection("item." + item + section)), p, openType != GUIOpenType.Editor, openType != GUIOpenType.Editor, openType != GUIOpenType.Editor);

            //Это для КАСТОМ ПРЕДМЕТОВ
            if(pconfig.contains("item." + item + section + ".itemType")) {
                //это для содержимого в разделе Тип предмета
                if (pconfig.getStringList("item." + item + section + ".itemType").contains("placeable") && openType == GUIOpenType.Refresh) {
                    //сохраняет предмет прежним, OpenType == 0 означает, что панель обновляется
                    setItem(p.getOpenInventory().getItem(Integer.parseInt(item)),Integer.parseInt(item),i,p,position);
                    takenSlots.add(Integer.parseInt(item));
                    continue;
                }
            }

            try {
                //помещение предмета в GUI
                setItem(s,Integer.parseInt(item),i,p,position);
                takenSlots.add(Integer.parseInt(item));
                //i.setItem(Integer.parseInt(item), s);
                //размещает повторяющиеся предметы только в режиме без редактора. Они являются просто визуальными и не будут передавать команды
                if(pconfig.contains("item." + item + section + ".duplicate") && openType != GUIOpenType.Editor) {
                    try {
                        String[] duplicateItems = pconfig.getString("item." + item + section + ".duplicate").split(",");
                        for (String tempDupe : duplicateItems) {
                            if (tempDupe.contains("-")) {
                                //если существует несколько элементов dupe, преобразует числа в int
                                int[] bothNumbers = new int[]{Integer.parseInt(tempDupe.split("-")[0]), Integer.parseInt(tempDupe.split("-")[1])};
                                for(int n = bothNumbers[0]; n <= bothNumbers[1]; n++){
                                    try{
                                        if(!pconfig.contains("item." + n)){
                                            setItem(s,n,i,p,position);
                                            takenSlots.add(n);
                                        }
                                    }catch(NullPointerException ignore){
                                        setItem(s,n,i,p,position);
                                        takenSlots.add(n);
                                    }
                                }
                            } else {
                                //если есть только один дюп предмет
                                try{
                                    if(!pconfig.contains("item." + Integer.parseInt(tempDupe))){
                                        setItem(s,Integer.parseInt(tempDupe),i,p,position);
                                        takenSlots.add(Integer.parseInt(tempDupe));
                                    }
                                }catch(NullPointerException ignore){
                                    setItem(s,Integer.parseInt(tempDupe),i,p,position);
                                    takenSlots.add(Integer.parseInt(tempDupe));
                                }
                            }
                        }
                    }catch(NullPointerException nullp){
                        plugin.debug(nullp,p);
                        p.closeInventory();
                        plugin.openGUIs.closeGUIForLoader(p.getName(),position);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ignore) {}
        }
        if (pconfig.contains("empty") && !Objects.equals(pconfig.getString("empty"), "AIR")) {
            ItemStack empty;
            try {
                //emptyID для более старых версий minecraft (возможно, позже они будут устаревшими)
                short id = 0;
                if(pconfig.contains("emptyID")){
                    id = Short.parseShort(pconfig.getString("emptyID"));
                }
                //использует кастом предмет, либо просто тип предмета
                if(pconfig.contains("custom-item." + pconfig.getString("empty"))){
                    empty = plugin.itemCreate.makeItemFromConfig(gui,position,pconfig.getConfigurationSection("custom-item." + pconfig.getString("empty")),p,true,true,true);
                }else{
                    empty = new ItemStack(Objects.requireNonNull(Material.matchMaterial(pconfig.getString("empty").toUpperCase())), 1,id);
                    empty = plugin.nbt.setNBT(empty);
                    ItemMeta renamedMeta = empty.getItemMeta();
                    assert renamedMeta != null;
                    renamedMeta.setDisplayName(" ");
                    empty.setItemMeta(renamedMeta);
                }
                if (empty.getType() != Material.AIR) {
                    for (int c = 0; getInvSize(i,position) > c; ++c) {
                        if (!takenSlots.contains(c)) {
                            //размещает пустые предметы только в том случае, если они не редактируются
                            if(openType != GUIOpenType.Editor) {
                                setItem(empty,c,i,p,position);
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException | NullPointerException var26) {
                plugin.debug(var26,p);
            }
        }
        if (openType == GUIOpenType.Normal) {
            //объявит старую гуищку закрытой
            if(plugin.openGUIs.hasGUIOpen(p.getName(),position)){
                plugin.openGUIs.getOpenGUI(p.getName(),position).isOpen = false;
            }
            //открывает новую гуишку
            plugin.openGUIs.skipGUIClose.add(p.getName());
            plugin.openGUIs.openGUIForLoader(p.getName(),gui,position);
            //только если для этого нужно открыть верхний инвентарь
            if(position == GUIPosition.Top) {
                p.openInventory(i);
            }
            plugin.openGUIs.skipGUIClose.remove(p.getName());
        } else if (openType == GUIOpenType.Editor) {
            //Редактор всегда будет находиться в верхней части гуишки
            p.openInventory(i);
        } else if (openType == GUIOpenType.Refresh) {
            //openType 0 просто обновит гуишку
            if(position == GUIPosition.Top) {
                plugin.legacy.setStorageContents(p, plugin.legacy.getStorageContents(i));
            }
        } else if (openType == GUIOpenType.Return) {
            //вернет инвентарь, не открывая его вообще
            return i;
        }
        return i;
    }

    private int getInvSize(Inventory inv, GUIPosition position){
        if(position == GUIPosition.Top){
            return inv.getSize();
        }else if(position == GUIPosition.Middle){
            return 27;
        }else{
            return 9;
        }
    }
    private void setItem(ItemStack item, int slot, Inventory inv, Player p, GUIPosition position) throws ArrayIndexOutOfBoundsException{
        if(position == GUIPosition.Top){
            inv.setItem(slot, item);
        }else if(position == GUIPosition.Middle){
            if(slot+9 < 36) {
                p.getInventory().setItem(slot + 9, item);
            }
        }else{
            if(slot < 9) {
                p.getInventory().setItem(slot, item);
            }
        }
    }

    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
