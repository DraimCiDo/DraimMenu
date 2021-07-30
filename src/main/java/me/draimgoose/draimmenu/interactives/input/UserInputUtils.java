package me.draimgoose.draimmenu.interactives.input;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.openguimanager.GUIPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class UserInputUtils implements Listener {
    DraimMenu plugin;
    public UserInputUtils(DraimMenu pl) {
        this.plugin = pl;
    }

    public HashMap<Player, PlayerInput> playerInput = new HashMap<>();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if(playerInput.containsKey(e.getPlayer())){
            e.setCancelled(true);
            if(e.getMessage().equalsIgnoreCase(plugin.config.getString("input.input-cancel"))){
                e.getPlayer().sendMessage(plugin.tex.colour( Objects.requireNonNull(plugin.config.getString("config.input-cancelled"))));
                playerInput.remove(e.getPlayer());
                return;
            }
            playerInput.get(e.getPlayer()).gui.placeholders.addPlaceholder("player-input",e.getMessage());

            //получение определенных слов из входных данных
            int c = 0;
            for(String message : e.getMessage().split("\\s")){
                playerInput.get(e.getPlayer()).gui.placeholders.addPlaceholder("player-input-" + (c+1),message);
                c++;
            }

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    plugin.commandTags.runCommands(playerInput.get(e.getPlayer()).gui, GUIPosition.Top,e.getPlayer(), playerInput.get(e.getPlayer()).commands); //Я должен сделать это, чтобы запускать регулярные пустоты Bukkit в асинхронном событии
                    playerInput.remove(e.getPlayer());
                }
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        //если проигрыватель находится в режиме генерации, удаляется режим генерации
        playerInput.remove(e.getPlayer());
    }

    public void sendMessage(GUI gui, GUIPosition pos, Player p){
        List<String> inputMessages = new ArrayList<>(plugin.config.getStringList("input.input-message"));
        for (String temp : inputMessages) {
            temp = temp.replaceAll("%dm-args%", Objects.requireNonNull(plugin.config.getString("input.input-cancel")));
            p.sendMessage(plugin.tex.placeholders(gui,pos,p, temp));
        }
    }
}
