package com.ccs.playersaver.playersaver;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Commands implements CommandExecutor {

    private final PlayerSaver plugin;

    public Commands(PlayerSaver plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ps <ip/player> <value>");
            return true;
        }

        String type = args[0].toLowerCase();
        String value = args[1];

        if (type.equals("ip")) {
            File playerFile = new File(plugin.getDataFolder(), "Ips/" + value + ".txt");
            displayLastLines(sender, playerFile);
        } else if (type.equals("player")) {
            File playerFile = new File(plugin.getDataFolder(), "players/" + value + ".txt");
            displayLastLines(sender, playerFile);
        } else if (type.equals("allips")) {
            // /ps allips <player>
            File playerFile = new File(plugin.getDataFolder(), "players/" + value + ".txt");
            displayAllIPs(sender, playerFile);
        } else if (type.equals("find")) {
            // /ps find <ip>
            findPlayersByIP(sender, value);
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid type. Use 'ip', 'player', 'allips' або 'find'.");
        }

        return true;
    }

    private void displayLastLines(CommandSender sender, File file) {
        if (!file.exists()) {
            sender.sendMessage(ChatColor.RED + "File not found: " + file.getName());
            return;
        }

        LinkedList<String> lines = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (lines.size() > 20) {
                    lines.removeFirst();
                }
            }
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error reading file: " + e.getMessage());
            return;
        }

        // Send the last lines to the sender
        if (lines.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No data found.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Last 20 lines:");
            for (String line : lines) {
                sender.sendMessage(ChatColor.WHITE + line);
            }
        }
    }

    private void displayAllIPs(CommandSender sender, File file) {
        if (!file.exists()) {
            sender.sendMessage(ChatColor.RED + "File not found: " + file.getName());
            return;
        }
        List<String> ips = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("IP: ")) {
                    String ip = line.split("IP: ")[1].split(" ")[0];
                    if (!ips.contains(ip)) ips.add(ip);
                }
            }
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error reading file: " + e.getMessage());
            return;
        }
        if (ips.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No IPs found.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "All IPs:");
            for (String ip : ips) {
                sender.sendMessage(ChatColor.WHITE + ip);
            }
        }
    }

    private void findPlayersByIP(CommandSender sender, String ip) {
        File playersFolder = new File(plugin.getDataFolder(), "players");
        File[] files = playersFolder.listFiles();
        if (files == null) {
            sender.sendMessage(ChatColor.RED + "No player data found.");
            return;
        }
        List<String> foundPlayers = new ArrayList<>();
        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("IP: " + ip + " ")) {
                        foundPlayers.add(file.getName().replace(".txt", ""));
                        break;
                    }
                }
            } catch (IOException ignored) {}
        }
        if (foundPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No players found for IP: " + ip);
        } else {
            sender.sendMessage(ChatColor.GREEN + "Players for IP " + ip + ":");
            for (String player : foundPlayers) {
                sender.sendMessage(ChatColor.WHITE + player);
            }
        }
    }
}
