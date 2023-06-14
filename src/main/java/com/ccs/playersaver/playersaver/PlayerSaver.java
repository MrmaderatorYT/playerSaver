package com.ccs.playersaver.playersaver;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public final class PlayerSaver extends JavaPlugin implements Listener{

    private Map<String, File> playerFiles;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        playerDataManager = new PlayerDataManager(this);
        playerFiles = new HashMap<>();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        String playerName = event.getPlayer().getName();
        String uuid = event.getPlayer().getUniqueId().toString();
        File playerFile = getPlayerFile(ip);
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try {
            FileWriter writer = new FileWriter(playerFile, true);
            writer.write("Nick: "+playerName + " UUID: "+uuid+" Time of connecting: "+time+"\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getPlayerFile(String ip) {
        if (!playerFiles.containsKey(ip)) {
            File dataFolder = new File(getDataFolder(), "Ips");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File playerFile = new File(dataFolder, ip + ".txt");
            if (!playerFile.exists()) {
                try {
                    playerFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            playerFiles.put(ip, playerFile);
        }
        return playerFiles.get(ip);
    }
}
