package com.example.advancedspawners.hologram;

import com.example.advancedspawners.config.PluginConfig;
import com.example.advancedspawners.model.SpawnerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HologramManager {
    private final Plugin plugin;
    private final PluginConfig config;
    private final Map<UUID, TextDisplay> displays = new HashMap<>();

    public HologramManager(Plugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void spawnAll(Collection<SpawnerData> spawners) {
        if (!config.hologramsEnabled()) return;
        for (SpawnerData data : spawners) {
            refresh(data);
        }
    }

    public void refreshAll(Collection<SpawnerData> spawners) {
        removeAll();
        spawnAll(spawners);
    }

    public void refresh(SpawnerData data) {
        if (!config.hologramsEnabled()) return;
        Location base = data.location().toLocation();
        if (base == null || base.getWorld() == null || !base.getWorld().isChunkLoaded(base.getBlockX() >> 4, base.getBlockZ() >> 4)) return;
        Location location = base.add(0.5, 1.65, 0.5);
        TextDisplay display = displays.get(data.id());
        if (display == null || !display.isValid()) {
            display = base.getWorld().spawn(location, TextDisplay.class, spawned -> {
                spawned.setPersistent(false);
                spawned.setBillboard(Display.Billboard.CENTER);
                spawned.setSeeThrough(false);
                spawned.setShadowed(true);
            });
            displays.put(data.id(), display);
        } else {
            display.teleport(location);
        }
        display.text(Component.text("Spawner " + data.entityType().name(), NamedTextColor.AQUA)
                .append(Component.newline())
                .append(Component.text("Fuel: " + fuelText(data) + " | Itens: " + data.storedItemCount(), NamedTextColor.GRAY)));
    }

    public void remove(UUID id) {
        TextDisplay display = displays.remove(id);
        if (display != null && display.isValid()) {
            display.remove();
        }
    }

    public void removeAll() {
        for (TextDisplay display : displays.values()) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        displays.clear();
    }

    private String fuelText(SpawnerData data) {
        if (data.upgrade(com.example.advancedspawners.model.UpgradeType.FUEL) >= config.maxLevel(com.example.advancedspawners.model.UpgradeType.FUEL)
                && config.fuelConsumptionMultiplier(data.upgrade(com.example.advancedspawners.model.UpgradeType.FUEL)) <= 0D) {
            return "infinito";
        }
        return String.valueOf(data.fuelTicks());
    }
}
