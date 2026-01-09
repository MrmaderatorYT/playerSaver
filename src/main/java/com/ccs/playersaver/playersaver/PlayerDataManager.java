package com.ccs.playersaver.playersaver;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PlayerDataManager implements Listener {

    private Map<String, File> playerFiles;
    private File playersFolder;
    private Map<String, Set<String>> playerIPs;
    private Map<String, String> ipGeoCache;

    public PlayerDataManager(JavaPlugin plugin) {
        playersFolder = new File(plugin.getDataFolder(), "players");
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
        playerFiles = new HashMap<>();
        playerIPs = new HashMap<>();
        ipGeoCache = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();
        String version = player.getClientBrandName() != null ? player.getClientBrandName() : "unknown";
        String worldName = player.getWorld().getName();
        String location = player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String geoLocation = getGeoLocation(ip);

        Set<String> ips = playerIPs.getOrDefault(playerName, new HashSet<>());
        if (!ips.contains(ip)) {
            ips.add(ip);
            playerIPs.put(playerName, ips);
        }
        File playerFile = getPlayerFile(playerName);
        try {
            FileWriter writer = new FileWriter(playerFile, true);
            writer.write("JOIN: IP: " + ip + " Version: " + version + " World: " + worldName + " Location: " + location + " Geo: " + geoLocation + " Time: " + time + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();
        String version = player.getClientBrandName() != null ? player.getClientBrandName() : "unknown";
        String worldName = player.getWorld().getName();
        String location = player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String geoLocation = getGeoLocation(ip);

        Set<String> ips = playerIPs.getOrDefault(playerName, new HashSet<>());
        if (!ips.contains(ip)) {
            ips.add(ip);
            playerIPs.put(playerName, ips);
        }
        File playerFile = getPlayerFile(playerName);
        try {
            FileWriter writer = new FileWriter(playerFile, true);
            writer.write("QUIT: IP: " + ip + " Version: " + version + " World: " + worldName + " Location: " + location + " Geo: " + geoLocation + " Time: " + time + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    private String getGeoLocation(String ip) {
        // Перевіряємо кеш спочатку
        if (ipGeoCache.containsKey(ip)) {
            return ipGeoCache.get(ip);
        }

        // Для локальних IP не робимо запит
        if (ip.equals("127.0.0.1") || ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
            String localGeo = "Local Network";
            ipGeoCache.put(ip, localGeo);
            return localGeo;
        }

        try {
            URL url = new URL("http://ip-api.com/json/" + ip + "?fields=country,city,status");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Парсимо простий JSON відповідь
                String jsonResponse = response.toString();
                String country = extractValue(jsonResponse, "country");
                String city = extractValue(jsonResponse, "city");

                String geoLocation = "Unknown";
                if (country != null && city != null) {
                    geoLocation = country + ", " + city;
                } else if (country != null) {
                    geoLocation = country;
                }

                ipGeoCache.put(ip, geoLocation);
                return geoLocation;
            }
        } catch (Exception e) {
            // В разі помилки повертаємо "Unknown"
        }

        String unknownGeo = "Unknown";
        ipGeoCache.put(ip, unknownGeo);
        return unknownGeo;
    }

    private String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;

        startIndex += searchKey.length();
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
        if (endIndex == -1) return null;

        String value = json.substring(startIndex, endIndex).trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.isEmpty() ? null : value;
    }
}