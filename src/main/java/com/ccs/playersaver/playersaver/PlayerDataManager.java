package com.ccs.playersaver.playersaver;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerDataManager implements Listener {

    private Map<String, File> playerFiles;
    private File playersFolder;
    private Map<String, Set<String>> playerIPs;

    public PlayerDataManager(JavaPlugin plugin) {
        playersFolder = new File(plugin.getDataFolder(), "players");
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
        playerFiles = new HashMap<>();
        playerIPs = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();
        Set<String> ips = playerIPs.getOrDefault(playerName, new HashSet<>());
        if (!ips.contains(ip)) {
            ips.add(ip);
            playerIPs.put(playerName, ips);
            File playerFile = getPlayerFile(playerName);
            try {
                FileWriter writer = new FileWriter(playerFile);
                for (String savedIP : ips) {
                    writer.write(savedIP + "\n");
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();
        Set<String> ips = playerIPs.getOrDefault(playerName, new HashSet<>());
        if (!ips.contains(ip)) {
            ips.add(ip);
            playerIPs.put(playerName, ips);
            File playerFile = getPlayerFile(playerName);
            try {
                FileWriter writer = new FileWriter(playerFile);
                for (String savedIP : ips) {
                    writer.write(savedIP + "\n");
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getPlayerFile(String playerName) {
        if (!playerFiles.containsKey(playerName)) {
            File playerFile = new File(playersFolder, playerName + ".txt");
            if (!playerFile.exists()) {
                try {
                    playerFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            playerFiles.put(playerName, playerFile);
        }
        return playerFiles.get(playerName);
    }
}