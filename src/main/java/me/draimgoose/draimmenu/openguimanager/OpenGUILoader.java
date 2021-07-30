package me.draimgoose.draimmenu.openguimanager;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.api.GUIClosedEvent;
import me.draimgoose.draimmenu.api.GUIInterface;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class OpenGUILoader {
    DraimMenu plugin;
    public OpenGUILoader(DraimMenu pl) {
        this.plugin = pl;
    }

    /*
    Это используется как менее медленный и не зависящий от названия способ определения
    того, какие менюшки открыты для конкретных игроков
    Раздел конфигурации открывается в правильную гуишку, поэтому нет необходимости в названии гуи
    */
    public HashMap<String, GUIInterface> openGUIs = new HashMap<>(); //имя игрока и интерфейс gui
    public HashSet<String> skipGUIClose = new HashSet<>(); //не удаляет игрока, если он есть в этом списке

    //это вернет gui CF в зависимости от игрока, если ее там нет, она вернет значение null в этом списке
    public GUI getOpenGUI(String playerName, GUIPosition position){
        for(Map.Entry<String, GUIInterface> entry : openGUIs.entrySet()){
            if(entry.getKey().equals(playerName)){
                return entry.getValue().getGui(position);
            }
        }
        return null;
    }

    //true если у игрока в локации открыта соответствующая gui
    public boolean hasGUIOpen(String playerName, String guiName, GUIPosition position){
        for(Map.Entry<String, GUIInterface> entry : openGUIs.entrySet()){
            try {
                if (entry.getKey().equals(playerName) && entry.getValue().getGui(position).getName().equals(guiName)) {
                    return true;
                }
            }catch (NullPointerException ex){
                return false;
            }
        }
        return false;
    }

    //true если у игрока открыта gui
    public boolean hasGUIOpen(String playerName, GUIPosition position) {
        for(Map.Entry<String, GUIInterface> entry : openGUIs.entrySet()){
            try {
                if(entry.getKey().equals(playerName) && entry.getValue().getGui(position) != null){
                    return true;
                }
            }catch (NullPointerException ex){
                return false;
            }
        }
        return false;
    }

    //сообщяет загрузчику, что gui была открыта
    public void openGUIForLoader(String playerName, GUI gui, GUIPosition position){
        if(!openGUIs.containsKey(playerName)){
            openGUIs.put(playerName, new GUIInterface(playerName));
        }
        openGUIs.get(playerName).setGui(gui,position);
        openGUIs.get(playerName).getGui(position).isOpen = true;
        if (plugin.config.contains("config.gui-snooper")) {
            if (Objects.requireNonNull(plugin.config.getString("config.gui-snooper")).trim().equalsIgnoreCase("true")) {
                Bukkit.getConsoleSender().sendMessage("(DraimMenu) " + playerName + " Открыто" + gui.getName() + " на " + position);
            }
        }
    }

    //закрывает все gui для игроков, которые открыты в данный момент
    public void closeGUIForLoader(String playerName, GUIPosition position){
        if(!openGUIs.containsKey(playerName) || skipGUIClose.contains(playerName)){
            return;
        }
        GUICloseCommands(playerName,position,openGUIs.get(playerName).getGui(position));
        if (plugin.config.contains("config.gui-snooper")) {
            if (Objects.requireNonNull(plugin.config.getString("config.gui-snooper")).equalsIgnoreCase("true")) {
                Bukkit.getConsoleSender().sendMessage("(DraimMenu) " + playerName + " Закрыто " + openGUIs.get(playerName).getGui(position).getName() + " на " + position);
            }
        }

        //событие с закрытыми gui
        GUIClosedEvent closedEvent = new GUIClosedEvent(Bukkit.getPlayer(playerName),openGUIs.get(playerName).getGui(position),position);
        Bukkit.getPluginManager().callEvent(closedEvent);

        openGUIs.get(playerName).setGui(null,position);
        //снимает, если все gui закрыты или если верхняя gui закрыта
        if(openGUIs.get(playerName).allClosed()){
            removePlayer(playerName);
        }else if(openGUIs.get(playerName).getGui(GUIPosition.Top) == null){
            removePlayer(playerName);
        }

        //приводит в порядок инвентарь
        plugin.inventorySaver.restoreInventory(Bukkit.getPlayer(playerName),position);
    }

    //удаляет игрока с карты открытых gui
    public void removePlayer(String playerName){
        openGUIs.get(playerName).setGui(null,GUIPosition.Top);
        openGUIs.get(playerName).setGui(null,GUIPosition.Middle);
        openGUIs.get(playerName).setGui(null,GUIPosition.Bottom);
        openGUIs.remove(playerName);
    }

    public void GUICloseCommands(String playerName,GUIPosition position, GUI gui){
        if (gui.getConfig().contains("commands-on-close")) {
            //выполнение команд при закрытии панели
            try {
                plugin.commandTags.runCommands(gui,position,Bukkit.getPlayer(playerName),gui.getConfig().getStringList("commands-on-close"));
            }catch(Exception s){
                plugin.debug(s,null);
            }
        }
    }

    public boolean isNBTInjected(ItemStack itm){
        if(itm != null){
            return plugin.nbt.hasNBT(itm);
        }
        return false;
    }
}
