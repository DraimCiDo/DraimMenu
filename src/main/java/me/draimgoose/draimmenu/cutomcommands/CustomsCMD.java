package me.draimgoose.draimmenu.cutomcommands;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class CustomsCMD implements Listener {
    DraimMenu plugin;
    public CustomsCMD(DraimMenu pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent e) {
        try {
            for (GUI gui : plugin.guiList) {
                if (gui.getConfig().contains("commands")) {
                    List<String> guiCommands = gui.getConfig().getStringList("commands");
                    for(String cmd : guiCommands){
                        if(cmd.equalsIgnoreCase(e.getMessage().replace("/", ""))){
                            e.setCancelled(true);
                            gui.copy().open(e.getPlayer(), GUIPosition.Top);
                            return;
                        }

                        boolean correctCommand = true;
                        ArrayList<String[]> placeholders = new ArrayList<>();
                        String[] phEnds = plugin.placeholders.getPlaceholderEnds(gui,true);
                        String[] command = cmd.split("\\s");
                        String[] message = e.getMessage().replace("/", "").split("\\s");

                        if(command.length != message.length){
                            continue;
                        }

                        for(int i = 0; i < cmd.split("\\s").length; i++){
                            if(command[i].startsWith(phEnds[0])){
                                placeholders.add(new String[]{command[i].replace(phEnds[0],"").replace(phEnds[1],""), message[i]});
                            }else if(!command[i].equals(message[i])){
                                correctCommand = false;
                            }
                        }

                        if(correctCommand){
                            e.setCancelled(true);
                            GUI openGUI = gui.copy();
                            for(String[] placeholder : placeholders){
                                openGUI.placeholders.addPlaceholder(placeholder[0],placeholder[1]);
                            }
                            openGUI.open(e.getPlayer(),GUIPosition.Top);
                            return;
                        }
                    }
                }
            }
        }catch(NullPointerException exc){
            plugin.debug(exc,e.getPlayer());
        }
    }
}
