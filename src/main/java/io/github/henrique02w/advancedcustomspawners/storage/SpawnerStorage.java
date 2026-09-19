package io.github.henrique02w.advancedcustomspawners.storage;

import io.github.henrique02w.advancedcustomspawners.model.BlockLocation;
import io.github.henrique02w.advancedcustomspawners.model.DropMode;
import io.github.henrique02w.advancedcustomspawners.model.SpawnerData;
import io.github.henrique02w.advancedcustomspawners.model.UpgradeType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class SpawnerStorage {
    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration dataFile;

    public SpawnerStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        reload();
    }

    public void reload() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        dataFile = YamlConfiguration.loadConfiguration(file);
    }

    public List<SpawnerData> loadAll() {
        List<SpawnerData> loaded = new ArrayList<>();
        ConfigurationSection root = dataFile.getConfigurationSection("spawners");
        if (root == null) return loaded;

        for (String idText : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(idText);
            if (section == null) continue;
            try {
                UUID id = UUID.fromString(idText);
                BlockLocation location = BlockLocation.fromSection(section.getConfigurationSection("location"));
                EntityType type = EntityType.valueOf(section.getString("type", "ZOMBIE").toUpperCase(Locale.ROOT));
                if (location == null) continue;
                SpawnerData data = new SpawnerData(id, location, type);
                data.enabled(section.getBoolean("enabled", true));
                for (UpgradeType upgrade : UpgradeType.values()) {
                    data.upgrade(upgrade, section.getInt("upgrades." + upgrade.key(), 0));
                }
                data.addFuel(section.getLong("fuel-ticks", 0L));
                data.dropMode(DropMode.valueOf(section.getString("drop-mode", "INTERNAL").toUpperCase(Locale.ROOT)));
                data.targetLocation(BlockLocation.fromSection(section.getConfigurationSection("target")));
                data.storage().addAll(readItems(section, "storage"));
                data.pendingRoute().addAll(readItems(section, "pending-route"));
                data.markClean();
                loaded.add(data);
            } catch (RuntimeException ex) {
                plugin.getLogger().warning("Could not load spawner " + idText + ": " + ex.getMessage());
            }
        }
        return loaded;
    }

    public void saveAll(Collection<SpawnerData> spawners, boolean force) {
        for (SpawnerData data : spawners) {
            if (!force && !data.dirty()) continue;
            String path = "spawners." + data.id();
            dataFile.set(path + ".location", data.location().serialize());
            dataFile.set(path + ".type", data.entityType().name());
            dataFile.set(path + ".enabled", data.enabled());
            for (UpgradeType type : UpgradeType.values()) {
                dataFile.set(path + ".upgrades." + type.key(), data.upgrade(type));
            }
            dataFile.set(path + ".fuel-ticks", data.fuelTicks());
            dataFile.set(path + ".drop-mode", data.dropMode().name());
            dataFile.set(path + ".target", data.targetLocation() == null ? null : data.targetLocation().serialize());
            dataFile.set(path + ".storage", data.storage());
            dataFile.set(path + ".pending-route", data.pendingRoute());
            data.markClean();
        }
        try {
            dataFile.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save data.yml: " + ex.getMessage());
        }
    }

    public void delete(UUID id) {
        dataFile.set("spawners." + id, null);
        try {
            dataFile.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save data.yml after delete: " + ex.getMessage());
        }
    }

    private List<ItemStack> readItems(ConfigurationSection section, String path) {
        List<ItemStack> items = new ArrayList<>();
        for (Object object : section.getList(path, List.of())) {
            if (object instanceof ItemStack item && !item.getType().isAir() && item.getAmount() > 0) {
                items.add(item.clone());
            }
        }
        return items;
    }
}
