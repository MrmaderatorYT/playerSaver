package com.ccs.playersaver.playersaver;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
public final class PlayerSaver extends JavaPlugin implements Listener{

    private Map<String, File> playerFiles;


    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        playerFiles = new HashMap<>();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (File file : playerFiles.values()) {
            file.delete();
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        String playerName = event.getPlayer().getName();
        File playerFile = getPlayerFile(ip);
        try {
            FileWriter writer = new FileWriter(playerFile, true);
            writer.write(playerName + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getPlayerFile(String ip) {
        if (!playerFiles.containsKey(ip)) {
            File dataFolder = getDataFolder();
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