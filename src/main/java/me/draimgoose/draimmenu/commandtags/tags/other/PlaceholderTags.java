package me.draimgoose.draimmenu.commandtags.tags.other;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.commandtags.CommandTagEvent;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlaceholderTags implements Listener {
    DraimMenu plugin;
    public PlaceholderTags(DraimMenu pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("placeholder=")) {
            e.commandTagUsed();
            String cmd;
            cmd = String.join(" ",e.raw);

            if(e.gui == null){
                return;
            }

            Character[] cm = ArrayUtils.toObject(cmd.toCharArray());
            for(int i = 0; i < cm.length; i++){
                if(cm[i].equals('[')){
                    String contents = cmd.substring(i+1, i+cmd.substring(i).indexOf(']'));
                    String placeholder = contents.substring(0,contents.indexOf(':'));
                    String value = plugin.tex.placeholders(e.gui,e.pos,e.p,contents.substring(contents.indexOf(':')+1));
                    e.gui.placeholders.addPlaceholder(placeholder,value);
                    i = i+contents.length()-1;
                }
            }
            return;
        }
        if(e.name.equalsIgnoreCase("add-placeholder=")) {
            e.commandTagUsed();
            String cmd;
            cmd = String.join(" ",e.raw);

            if (e.gui == null) {
                return;
            }

            Character[] cm = ArrayUtils.toObject(cmd.toCharArray());
            for (int i = 0; i < cm.length; i++) {
                if (cm[i].equals('[')) {
                    String contents = cmd.substring(i + 1, i + cmd.substring(i).indexOf(']'));
                    String placeholder = contents.substring(0, contents.indexOf(':'));
                    if (!e.gui.placeholders.keys.containsKey(placeholder)) {
                        String value = plugin.tex.placeholders(e.gui,e.pos, e.p, contents.substring(contents.indexOf(':') + 1));
                        e.gui.placeholders.addPlaceholder(placeholder, value);
                    }
                    i = i + contents.length() - 1;
                }
            }
        }
    }
}
