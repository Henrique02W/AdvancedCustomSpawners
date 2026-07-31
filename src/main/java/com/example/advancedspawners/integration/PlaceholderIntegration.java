package com.example.advancedspawners.integration;

import com.example.advancedspawners.service.SpawnerManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class PlaceholderIntegration extends PlaceholderExpansion {
    private final Plugin plugin;
    private final SpawnerManager spawnerManager;

    private PlaceholderIntegration(Plugin plugin, SpawnerManager spawnerManager) {
        this.plugin = plugin;
        this.spawnerManager = spawnerManager;
    }

    public static void register(Plugin plugin, SpawnerManager spawnerManager) {
        new PlaceholderIntegration(plugin, spawnerManager).register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "advancedspawners";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        return switch (params.toLowerCase()) {
            case "total" -> String.valueOf(spawnerManager.all().size());
            case "stored_items" -> String.valueOf(spawnerManager.all().stream().mapToInt(data -> data.storedItemCount()).sum());
            case "pending_routes" -> String.valueOf(spawnerManager.all().stream().mapToInt(data -> data.pendingRoute().size()).sum());
            default -> null;
        };
    }
}
