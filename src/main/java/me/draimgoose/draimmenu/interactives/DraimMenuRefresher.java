package me.draimgoose.draimmenu.interactives;

import me.draimgoose.draimmenu.DraimMenu;
import me.draimgoose.draimmenu.api.GUI;
import me.draimgoose.draimmenu.api.GUIOpenedEvent;
import me.draimgoose.draimmenu.openguimanager.GUIOpenType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class DraimMenuRefresher implements Listener {
    DraimMenu plugin;
    public DraimMenuRefresher(DraimMenu pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onGUIOpen(GUIOpenedEvent e){ //Обрабатывает, когда игроки открывают инвентарь
        if (plugin.config.contains("config.refresh-guis")) {
            if (Objects.requireNonNull(plugin.config.getString("config.refresh-guis")).trim().equalsIgnoreCase("false")) {
                return;
            }
        }

        Player p = e.getPlayer();
        GUI pn = e.getGui();

        if(pn.getConfig().contains("sound-on-open")){
            if(Bukkit.getVersion().contains("1.8")){
                pn.getConfig().set("sound-on-open", null);
            }
        }

        //если панель имеет кастом задержку обновления
        int tempRefreshDelay = plugin.config.getInt("config.refresh-delay");
        if(pn.getConfig().contains("refresh-delay")){
            tempRefreshDelay = pn.getConfig().getInt("refresh-delay");
        }
        final int refreshDelay = tempRefreshDelay;

        if(pn.getConfig().contains("guiType")) {
            if (pn.getConfig().getStringList("guiType").contains("static")) {
                //не обновляет временные gui, только gui по умолчанию
                return;
            }
        }

        new BukkitRunnable(){
            int c = 0;
            int animatecount = 0;
            @Override
            public void run() {
                int animatevalue = -1;
                if(pn.getConfig().contains("animatevalue")){
                    animatevalue = pn.getConfig().getInt("animatevalue");
                }
                //счетчик отсчитывает задержку обновления (в секундах), затем перезапускается
                if(c < refreshDelay){
                    c+=1;
                }else{
                    c=0;
                }
                //обновление
                if(e.getGUI().isOpen){
                    if(p.getOpenInventory().getTopInventory().getHolder() != p){
                        //если открытый инвентарь не является панелью (принадлежит владельцу игрока), отменяет
                        this.cancel();
                        return;
                    }

                    if(c == 0) {
                        //счетчик анимации
                        if(animatevalue != -1) {
                            if (animatecount < animatevalue) {
                                animatecount += 1;
                            } else {
                                animatecount = 0;
                            }
                        }
                        try {
                            if(plugin.debug.isEnabled(p)){
                                //перезагружает gui, включена ли отладка (только личная отладка)
                                pn.setConfig(YamlConfiguration.loadConfiguration(pn.getFile()));
                            }
                            plugin.createGUI.openGui(pn, p,e.getPosition(), GUIOpenType.Refresh,animatecount);
                        } catch (Exception ex) {
                            //ошибка при открытии gui
                            p.closeInventory();
                            plugin.openGUIs.closeGUIForLoader(p.getName(),e.getPosition());
                            this.cancel();
                        }
                    }
                }else{
                    if(Objects.requireNonNull(plugin.config.getString("config.stop-sound")).trim().equalsIgnoreCase("true")){
                        try {
                            p.stopSound(Sound.valueOf(Objects.requireNonNull(pn.getConfig().getString("sound-on-open")).toUpperCase()));
                        }catch(Exception sou){
                            //скип
                        }
                    }
                    c = 0;
                    this.cancel();
                    //удаление дубликатов предметов
                    p.updateInventory();
                    if(plugin.inventorySaver.hasNormalInventory(p)) {
                        for (ItemStack itm : p.getInventory().getContents()) {
                            if (itm != null) {
                                if (plugin.nbt.hasNBT(itm)) {
                                    p.getInventory().remove(itm);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 1,1); //20 тиков == 1 секунда (5 тиков = 0,25 секунды)
    }
}
