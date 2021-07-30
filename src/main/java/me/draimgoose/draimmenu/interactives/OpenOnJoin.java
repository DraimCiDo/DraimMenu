package me.draimgoose.draimmenu.interactives;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class OpenOnJoin implements Listener {
    DraimMenu plugin;
    public OpenOnJoin(DraimMenu pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onWorldLogin(PlayerJoinEvent e){
        //открывается только при первом входе игрока в систему
        openOnJoin(e.getPlayer(),"open-on-login.");
    }

    @EventHandler
    public void onWorldJoin(PlayerChangedWorldEvent e){
        //открывается только тогда, когда игрок меняет мир
        openOnJoin(e.getPlayer(),"open-on-join.");
    }

    private void openOnJoin(Player p, String joinType){
        if(plugin.config.contains(joinType + p.getWorld().getName())){
            String command = "open= " + plugin.config.getString(joinType + p.getWorld().getName());
            plugin.commandTags.runCommand(null, GUIPosition.Top,p, command);
        }
    }
}
