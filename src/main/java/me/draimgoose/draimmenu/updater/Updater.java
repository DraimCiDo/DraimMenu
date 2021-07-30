package me.draimgoose.draimmenu.updater;

import me.draimgoose.draimmenu.DraimMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;

public class Updater implements Listener {
    DraimMenu plugin;
    public Updater(DraimMenu pl) {
        this.plugin = pl;
    }

    //если установлено на что-то, плуг загрузит эту версию при перезапуске.
    //Это может быть номер версии, "latest" или "cancel".
    public String downloadVersionManually = null;
    public String catchedLatestVersion = "null";

    //отправляет сообщение об обновлении, когда игрок присоединится к игре с пермишкой
    @EventHandler
    public void joinGame(PlayerJoinEvent e){
        if(e.getPlayer().hasPermission("draimmenu.update") && plugin.config.getBoolean("updater.update-checks")){
            if(githubNewUpdate(false)){
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.tex.sendMessage(e.getPlayer(),ChatColor.YELLOW + "Новое обновление доступно для скачивания!");
                        plugin.tex.sendString(e.getPlayer(),ChatColor.YELLOW
                                + "Текущая версия "
                                + ChatColor.RED + plugin.getDescription().getVersion() + ChatColor.YELLOW
                                + " Последняя версия " + ChatColor.GREEN + catchedLatestVersion);
                        this.cancel();
                    }
                }.runTaskTimer(plugin, 30, 1); //20 тиков == 1 сек
            }
        }
    }

    public boolean githubNewUpdate(boolean sendMessages){
        //обноление до последней версии
        getLatestVersion(sendMessages);

        if(plugin.getDescription().getVersion().contains("-")){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("DraimMenu" + ChatColor.GREEN + " Запущен на модифированной версии.");
            }
            return false;
        }

        //если обновление стоит на true, будет показ о новом обновление
        boolean update = !catchedLatestVersion.equals(plugin.getDescription().getVersion());

        if(update){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("(DraimMenu)" + ChatColor.GOLD + " ================================================");
                Bukkit.getConsoleSender().sendMessage("(DraimMenu)" + ChatColor.AQUA + " Доступно обновление для DraimMenu.");
                Bukkit.getConsoleSender().sendMessage("(DraimMenu)" + " Для скачивания DraimMenu " + ChatColor.GOLD + catchedLatestVersion + ChatColor.WHITE + " используй");
                Bukkit.getConsoleSender().sendMessage("(DraimMenu)" + ChatColor.WHITE + " команду:" + ChatColor.AQUA + " /dmv latest" + ChatColor.WHITE + " и презапусти сервер");
                Bukkit.getConsoleSender().sendMessage("(DraimMenu)" + ChatColor.GOLD + " ================================================");
            }
            return true;
        }
        return false;
    }

    public String getLatestVersion(boolean sendMessages){
        //проверка при null
        if(catchedLatestVersion.equals("null")){
            catchedLatestVersion = plugin.getDescription().getVersion();
        }

        //использование массива позволяет редактировать его в процессе доработки
        new BukkitRunnable(){
            public void run(){
                HttpURLConnection connection;
                try {
                    connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/DraimCiDo/DraimMenu/master/resource/plugin.yml").openConnection();
                    connection.connect();
                    catchedLatestVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine().split("\\s")[1];
                    connection.disconnect();
                } catch (IOException ignore) {
                    Bukkit.getConsoleSender().sendMessage("(DraimMenu)" + ChatColor.RED + " Не удалось получить доступ к github.");
                }
            }
        }.runTask(plugin);

        if(catchedLatestVersion.contains("-")){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("(DraimMenu)" + ChatColor.RED + " Не удается проверить наличие обновлений.");
            }
        }
        return catchedLatestVersion;
    }

    //имя плагина может быть получено только из основного класса
    public void autoUpdatePlugin(String pluginFileName){
        String latestVersion = catchedLatestVersion;
        String thisVersion = plugin.getDescription().getVersion();

        if(downloadVersionManually != null) {
            if (downloadVersionManually.equals("latest")) {
                downloadFile(latestVersion, pluginFileName);
            }else{
                downloadFile(downloadVersionManually, pluginFileName);
            }
            return;
        }

        if(latestVersion.equals(thisVersion) || thisVersion.contains("-")){
            //не нужно обновлять или запускать пользовательскую версию
            return;
        }
        if (Objects.requireNonNull(plugin.config.getString("updater.auto-update")).equalsIgnoreCase("true")) {
            //не обновляет плагин автоматически, если он отключен
            return;
        }
        if(Objects.equals(plugin.config.getString("updater.minor-updates-only"), "true")){
            //обновляет онли те версии, которые не будут ломаться
            if(thisVersion.split("\\.")[1].equals(latestVersion.split("\\.")[1]) && thisVersion.split("\\.")[0].equals(latestVersion.split("\\.")[0])){
                //первый и второй номера версии одинаковы, обновления: [major.major.minor.minor]
                downloadFile(latestVersion,pluginFileName);
            }
        }else{
            downloadFile(latestVersion,pluginFileName);
        }
    }

    // Процесс скачки с гита
    private void downloadFile(String latestVersion, String pluginFileName) {
        BufferedInputStream in = null;
        FileOutputStream fout = null;

        try {
            this.plugin.getLogger().info("Установка нового DraimMenu " + "v" + latestVersion);
            URL fileUrl = new URL("https://github.com/DraimCiDo/DraimMenu/releases/download/" + latestVersion + "/DraimMenu.jar");
            int fileLength = fileUrl.openConnection().getContentLength();
            in = new BufferedInputStream(fileUrl.openStream());
            fout = new FileOutputStream(new File(new File(".").getAbsolutePath() + "/plugins/", pluginFileName));
            byte[] data = new byte[1024];

            long downloaded = 0L;

            int count;
            while((count = in.read(data, 0, 1024)) != -1) {
                downloaded += count;
                fout.write(data, 0, count);
                int percent = (int)(downloaded * 100L / (long)fileLength);
                if (percent % 10 == 0) {
                    this.plugin.getLogger().info("Загрузка обновления: " + percent + "% из " + fileLength + " байтов.");
                }
            }
            this.plugin.getLogger().info("Обновление завершено");
        } catch (Exception var22) {
            this.plugin.getLogger().log(Level.WARNING, "Не удалось загрузить обновление.", var22);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException var21) {
                this.plugin.getLogger().log(Level.SEVERE, null, var21);
            }

            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException var20) {
                this.plugin.getLogger().log(Level.SEVERE, null, var20);
            }

        }

    }
}
