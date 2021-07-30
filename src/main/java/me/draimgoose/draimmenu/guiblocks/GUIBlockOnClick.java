package me.draimgoose.draimmenu.guiblocks;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class GUIBlockOnClick implements Listener {
    DraimMenu plugin;
    public GUIBlockOnClick(DraimMenu pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        if(Objects.requireNonNull(plugin.config.getString("config.gui-blocks")).equalsIgnoreCase("false")){
            return;
        }
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = e.getClickedBlock();
        Player p = e.getPlayer();
        assert block != null;
        if(!plugin.blockConfig.contains("blocks")){
            return;
        }
        if(plugin.openGUIs.hasGUIOpen(p.getName(), GUIPosition.Top)) {
            return;
        }
        for (String configLocation : Objects.requireNonNull(plugin.blockConfig.getConfigurationSection("blocks")).getKeys(false)) {
            String[] loc = configLocation.split("_");
            Location tempLocation = new Location(plugin.getServer().getWorld(loc[0].replaceAll("%dash%","_")),Double.parseDouble(loc[1]),Double.parseDouble(loc[2]),Double.parseDouble(loc[3]));
            if(tempLocation.equals(block.getLocation())){
                e.setCancelled(true);
                if(plugin.blockConfig.contains("blocks." + configLocation + ".commands")){
                    for(String command : plugin.blockConfig.getStringList("blocks." + configLocation + ".commands")){
                        plugin.commandTags.runCommand(null,GUIPosition.Top,p, command);
                    }
                    return;
                }
                String command = "open= " + plugin.blockConfig.getString("blocks." + configLocation + ".gui");
                plugin.commandTags.runCommand(null,GUIPosition.Top,p, command);
            }
        }
    }
}
