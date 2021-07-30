package me.draimgoose.draimmenu.commandtags.tags.other;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.commandtags.CommandTagEvent;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class SpecialTags implements Listener {
    DraimMenu plugin;
    public SpecialTags(DraimMenu pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("open=")) {
            e.commandTagUsed();
            String guiName = e.args[0];
            String cmd = String.join(" ",e.args).replace(e.args[0] + " ","").trim();

            GUI openGUIs = null;
            GUIPosition openPosition = e.pos;
            for(GUI pane : plugin.guiList){
                if(pane.getName().equals(guiName)){
                    openGUIs = pane.copy();
                }
            }
            if(openGUIs == null){
                return;
            }

            Character[] cm = ArrayUtils.toObject(cmd.toCharArray());
            for(int i = 0; i < cm.length; i++){
                if(cm[i].equals('[')){
                    String contents = cmd.substring(i+1, i+cmd.substring(i).indexOf(']'));
                    String placeholder = contents.substring(0,contents.indexOf(':'));
                    String value = plugin.tex.placeholders(e.gui,e.pos,e.p,contents.substring(contents.indexOf(':')+1));
                    openGUIs.placeholders.addPlaceholder(placeholder,value);
                    i = i+contents.length()-1;
                }else if(cm[i].equals('{')){
                    String contents = cmd.substring(i+1, i+cmd.substring(i).indexOf('}'));
                    openPosition = GUIPosition.valueOf(contents);
                    i = i+contents.length()-1;
                }
            }
            openGUIs.open(e.p,openPosition);
            return;
        }
        if(e.name.equalsIgnoreCase("close=")) {
            e.commandTagUsed();
            GUIPosition position = GUIPosition.valueOf(e.args[0]);
            if(position == GUIPosition.Middle && plugin.openGUIs.hasGUIOpen(e.p.getName(),position)){
                plugin.openGUIs.closeGUIForLoader(e.p.getName(),GUIPosition.Middle);
            }else if(position == GUIPosition.Bottom && plugin.openGUIs.hasGUIOpen(e.p.getName(),position)){
                plugin.openGUIs.closeGUIForLoader(e.p.getName(),GUIPosition.Bottom);
            }else if(position == GUIPosition.Top && plugin.openGUIs.hasGUIOpen(e.p.getName(),position)){
                e.p.closeInventory();
            }
            return;
        }
        if(e.name.equalsIgnoreCase("teleport=")) {
            e.commandTagUsed();
            if (e.args.length == 5) {
                float x, y, z, yaw, pitch;
                x = Float.parseFloat(e.args[0]);
                y = Float.parseFloat(e.args[1]);
                z = Float.parseFloat(e.args[2]);
                yaw = Float.parseFloat(e.args[3]);
                pitch = Float.parseFloat(e.args[4]);
                e.p.teleport(new Location(e.p.getWorld(), x, y, z, yaw, pitch));
            } else if (e.args.length <= 3) {
                float x, y, z;
                x = Float.parseFloat(e.args[0]);
                y = Float.parseFloat(e.args[1]);
                z = Float.parseFloat(e.args[2]);
                e.p.teleport(new Location(e.p.getWorld(), x, y, z));
            } else {
                try {
                    Player otherplayer = Bukkit.getPlayer(e.args[3]);
                    float x, y, z;
                    x = Float.parseFloat(e.args[0]);
                    y = Float.parseFloat(e.args[1]);
                    z = Float.parseFloat(e.args[2]);
                    assert otherplayer != null;
                    otherplayer.teleport(new Location(otherplayer.getWorld(), x, y, z));
                } catch (Exception tpe) {
                    plugin.tex.sendMessage(e.p,plugin.config.getString("config.format.notitem"));
                }
            }
            return;
        }
        if(e.name.equalsIgnoreCase("delay=")) {
            e.commandTagUsed();
            final int delayTicks = Integer.parseInt(e.args[0]);
            String finalCommand = String.join(" ",e.args).replace(e.args[0],"").trim();
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        plugin.commandTags.runCommand(e.gui,e.pos, e.p, finalCommand);
                    } catch (Exception ex) {
                        plugin.debug(ex, e.p);
                        this.cancel();
                    }
                    this.cancel();
                }
            }.runTaskTimer(plugin, delayTicks, 1);
        }
    }
}
