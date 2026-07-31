package com.example.advancedspawners.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MessageService {
    private final JavaPlugin plugin;
    private FileConfiguration messages;
    private FileConfiguration defaults;
    private String prefix;

    public MessageService(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
        defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("messages.yml"), StandardCharsets.UTF_8));
        prefix = messages.getString("prefix", "");
        if (prefix.isBlank()) {
            prefix = defaults.getString("prefix", "");
        }
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String raw = messages.getString(key, defaults.getString(key, "&cMensagem ausente: " + key));
        sender.sendMessage(component(prefix + replace(raw, placeholders)));
    }

    public void sendList(CommandSender sender, String key, Map<String, String> placeholders) {
        List<String> lines = messages.getStringList(key);
        if (lines.isEmpty()) {
            lines = defaults.getStringList(key);
        }
        for (String line : lines) {
            sender.sendMessage(component(replace(line, placeholders)));
        }
    }

    public Component component(String raw) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw == null ? "" : raw);
    }

    public Component text(String raw, Map<String, String> placeholders) {
        return component(replace(raw, placeholders));
    }

    private String replace(String raw, Map<String, String> placeholders) {
        String result = raw == null ? "" : raw;
        Map<String, String> all = new HashMap<>(placeholders);
        for (Map.Entry<String, String> entry : all.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
