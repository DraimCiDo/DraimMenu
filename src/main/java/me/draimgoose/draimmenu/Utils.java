package me.draimgoose.draimmenu;

import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.interactives.input.PlayerInput;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

import java.util.List;
import java.util.Objects;

public class Utils implements Listener {
    DraimMenu plugin;
    public Utils(DraimMenu pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void onItemDrag(InventoryDragEvent e) {
        Player p = (Player)e.getWhoClicked();
        if(!plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Top)){
            return;
        }
        if(e.getInventory().getType() != InventoryType.PLAYER){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent e) {
        // при нажатии на панель
        Player p = (Player)e.getWhoClicked();
        int clickedSlot = e.getSlot();

        if(!plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Top) || e.getClick() == ClickType.DOUBLE_CLICK){
            return;
        }

        // установка панели на верхнюю панель
        GUI gui = plugin.openGUIs.getOpenGUI(p.getName(),GUIPosition.Top);

        if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY){
            e.setCancelled(true);
        }

        if(e.getSlotType() == InventoryType.SlotType.OUTSIDE){
            // если щелкнуть по панели во внешней области графического интерфейса
            if (gui.getConfig().contains("outside-commands")) {
                try {
                    plugin.commandTags.runCommands(gui,GUIPosition.Top,p, gui.getConfig().getStringList("outside-commands"),e.getClick());
                }catch(Exception s){
                    plugin.debug(s,p);
                }
            }
            return;
        }

        GUIPosition position = GUIPosition.Top;
        if(e.getClickedInventory().getType() == InventoryType.PLAYER) {
            if (e.getSlotType() == InventoryType.SlotType.CONTAINER) {
                if(plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Middle)) {
                    position = GUIPosition.Middle;
                    clickedSlot -= 9;
                }else{
                    e.setCancelled(itemsUnmovable(gui));
                    return;
                }
            } else{
                if(plugin.openGUIs.hasGUIOpen(p.getName(),GUIPosition.Bottom)) {
                    position = GUIPosition.Bottom;
                    //это значение равно отменено, как если бы команда должна закрыть панель,
                    //и в том же слоте находится элемент горячей панели, он также активирует
                    //элемент горячей панели после закрытия панели
                    e.setCancelled(true);
                }else{
                    e.setCancelled(itemsUnmovable(gui));
                    return;
                }
            }
        }

        //правильное положение панелей
        gui = plugin.openGUIs.getOpenGUI(p.getName(),position);

        //это повторяет все элементы на панели
        boolean foundSlot = false;
        for(String slot : Objects.requireNonNull(gui.getConfig().getConfigurationSection("item")).getKeys(false)){
            if (slot.equals(Integer.toString(clickedSlot))) {
                foundSlot = true;
                break;
            }
        }
        if(!foundSlot){
            e.setCancelled(true);
            return;
        }

        //получиние раздел слота, на который был нажат
        String section = plugin.itemCreate.hasSection(gui,position,gui.getConfig().getConfigurationSection("item." + clickedSlot), p);

        if(gui.getConfig().contains("item." + clickedSlot + section + ".itemType")){
            if(gui.getConfig().getStringList("item." + clickedSlot + section + ".itemType").contains("placeable")){
                //пропустить, если элемент можно разместить
                e.setCancelled(false);
                return;
            }
        }

        e.setCancelled(true);
        p.updateInventory();

        //если у элемента есть область для ввода вместо команд
        if(gui.getConfig().contains("item." + clickedSlot + section + ".player-input")) {
            plugin.inputUtils.playerInput.put(p,new PlayerInput(gui,gui.getConfig().getStringList("item." + clickedSlot + section + ".player-input")));
            plugin.inputUtils.sendMessage(gui,position,p);
        }

        if(gui.getConfig().contains("item." + clickedSlot + section + ".commands")) {
            List<String> commands = gui.getConfig().getStringList("item." + clickedSlot + section + ".commands");
            if (commands.size() != 0) {
                //это заменит команду тега последовательности командами из последовательности
                List<String> commandsAfterSequence = commands;
                for (int i = 0; commands.size() - 1 >= i; i++) {
                    if(commands.get(i).startsWith("sequence=")){
                        String locationOfSequence = commands.get(i).split("\\s")[1];
                        List<String> commandsSequence = gui.getConfig().getStringList(locationOfSequence);
                        commandsAfterSequence.remove(i);
                        commandsAfterSequence.addAll(i,commandsSequence);
                    }
                }
                commands = commandsAfterSequence;

                for (int i = 0; commands.size() > i; i++) {
                    try {
                        commands.set(i, commands.get(i).replaceAll("%cp-clicked%", e.getCurrentItem().getType().toString()));
                    } catch (Exception mate) {
                        commands.set(i, commands.get(i).replaceAll("%cp-clicked%", "AIR"));
                    }
                }

                plugin.commandTags.runCommands(gui,position,p,commands,e.getClick());
            }
        }
    }

    private boolean itemsUnmovable(GUI gui){
        if(gui.getConfig().isSet("guiType")){
            //отменяет событие и возвращает к сигналу, никаких команд и никакого движения не произойдет
            return gui.getConfig().getStringList("guiType").contains("unmovable");
        }
        return false;
    }
}
