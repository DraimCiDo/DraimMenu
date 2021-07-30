package me.draimgoose.draimmenu.openwithitem;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class HotbarItemLoader {
    DraimMenu plugin;
    public HotbarItemLoader(DraimMenu pl) {
        this.plugin = pl;
    }

    // Слоты от 0 до 8 для хотбара, в которой используются 9-35 для внутр. инв
    HashMap<UUID, HotbarItemLoader> stationaryItems = new HashMap<>();

    // Компиляция списка массивов (слот 0-4 и индекс имён гуишек)
    public void reloadHotbarSlots() {
        stationaryItems.clear();
        // Обновление элементов хотбара для всех игроков при релоаде
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            plugin.hotbar.updateHotbarItems(p);
        }
    }

    //возвращает если найдено true
    public boolean stationaryExecute(int slot, Player p, ClickType click, boolean openGUI){
        if(stationaryItems.get(p.getUniqueId()).list.containsKey(slot)){
            if(openGUI) {
                try {
                    if (!plugin.nbt.getNBT(p.getInventory().getItem(slot), "DraimMenuHotbar").split(":")[1].equals(String.valueOf(slot))) {
                        return false;
                    }
                }catch(Exception ex){
                    return false;
                }
                GUI gui = stationaryItems.get(p.getUniqueId()).getGUI(slot);
                //автоматически открывает гуи только в том случае, если нет команд и мир игроков не отключен
                if(!p.hasPermission("draimmemnu.gui." + gui.getConfig().getString("perm"))){
                    return false;
                }
                if(!itemCheckExecute(p.getInventory().getItem(slot),p,false,false)){
                    return false;
                }
                if(gui.getHotbarSection(p).contains("commands")){
                    plugin.commandTags.runCommands(gui,GUIPosition.Top,p,gui.getHotbarSection(p).getStringList("commands"),click);
                    return true;
                }
                gui.open(p, GUIPosition.Top);
            }
            return true;
        }
        return false;
    }

    //возвращает если найдено true
    public boolean itemCheckExecute(ItemStack invItem, Player p, boolean openGUI, boolean stationaryOnly){
        try {
            if (plugin.nbt.getNBT(invItem, "DraimMenuHotbar") == null) {
                return false;
            }
        }catch(IllegalArgumentException | NullPointerException nu){
            return false;
        }
        for(GUI gui : plugin.guiList) {
            if(stationaryOnly){
                try {
                    if (plugin.nbt.getNBT(invItem, "DraimMenuHotbar").split(":")[1].equals("-1")) {
                        continue;
                    }
                }catch(NullPointerException | IllegalArgumentException ignore){}
            }
            if(gui.hasHotbarItem()){
                if(plugin.nbt.getNBT(invItem,"DraimMenuHotbar").split(":")[0].equals(gui.getName())){
                    if(openGUI) {
                        //автоматически открывает гуи только в том случае, если нет команд и мир игроков не отключен
                        if(!plugin.guiPerms.isGUIWorldEnabled(p,gui.getConfig())){
                            return false;
                        }
                        if(gui.getHotbarSection(p).contains("commands")){
                            for(String command : gui.getHotbarSection(p).getStringList("commands")){
                                plugin.commandTags.runCommand(gui,GUIPosition.Top,p, command);
                            }
                            return true;
                        }
                        gui.open(p,GUIPosition.Top);
                    }
                    return true;
                }
            }
        }
        return false;
    }


    public void updateHotbarItems(Player p) {
        /*
            Если игрок использует disabled-world/enabled-worlds
             и они изменяют сам мир, то это проверяет, может ли игрок получить предмет
             и если игрок может - даётся предмет(логично блять). Это происходит потому что,
             onRespawn не отдаёт игроку предмет во всех мирах, в которых он мог бы быть автоматом.

            Игроку конечно понадобится плагин для разделение инв между мирами
             чтобы это вступило в силу, но кого это ебёт. Я не хочу удалять предметы в хуевых мирах
             потому что тогда он может перезаписть один из реальных слотов при возвращении в enabled-worlds.
         */
        if(!plugin.openWithItem){
            // если нету открытых гуишек с такой хуйней как open-with-item
            return;
        }

        // Удаление старых предметов в хотбаре
        stationaryItems.put(p.getUniqueId(), new HotbarItemLoader());
        for(int i = 0; i <= 35; i++){
            try {
                if (plugin.nbt.getNBT(p.getInventory().getItem(i), "DraimMenuHotbar") != null) {
                    p.getInventory().setItem(i, new ItemStack(Material.AIR));
                }
            }catch(NullPointerException | IllegalArgumentException ignore){}
        }

        // Добавление новых предметов в хотбар
        for(GUI gui : plugin.guiList) {
            if(!plugin.guiPerms.isGUIWorldEnabled(p,gui.getConfig())){ // Будет чекать все файлы в папке
                continue;
            }
            if(p.hasPermission("draimemenu.gui." + gui.getConfig().getStings("perm")) && gui.hasHotbarItem()) {
                ItemStack s = gui.getHotbarItem(p);
                if(gui.getHotbarSection(p).contains("stationary")) {

                }
            }
        }
        p.updateInventory();
    }
}
