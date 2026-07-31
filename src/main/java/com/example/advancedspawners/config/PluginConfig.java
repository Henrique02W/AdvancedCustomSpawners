package com.example.advancedspawners.config;

import com.example.advancedspawners.model.FallbackMode;
import com.example.advancedspawners.model.UpgradeType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class PluginConfig {
    public record GuiItemDefinition(Material material, int customModelData) {}

    private final JavaPlugin plugin;
    private FileConfiguration defaults;
    private final Map<Material, FuelDefinition> fuels = new HashMap<>();
    private final EnumMap<UpgradeType, Integer> maxLevels = new EnumMap<>(UpgradeType.class);
    private Set<EntityType> blockedMobs = EnumSet.noneOf(EntityType.class);
    private final Map<String, GuiItemDefinition> guiItems = new HashMap<>();

    public PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("config.yml"), StandardCharsets.UTF_8));
        fuels.clear();
        ConfigurationSection fuelSection = section("fuel.items");
        if (fuelSection != null) {
            for (String key : fuelSection.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material == null) continue;
                long duration = longValue("fuel.items." + key + ".duration", 0L);
                double efficiency = Math.max(0.01D, doubleValue("fuel.items." + key + ".efficiency", 1.0D));
                fuels.put(material, new FuelDefinition(duration, efficiency));
            }
        }

        for (UpgradeType type : UpgradeType.values()) {
            maxLevels.put(type, Math.max(0, intValue("upgrades." + type.key() + ".max-level", 0)));
        }

        EnumSet<EntityType> loaded = EnumSet.noneOf(EntityType.class);
        List<String> blockedNames = stringList("settings.blocked-mobs");
        if (blockedNames.isEmpty()) {
            blockedNames = stringList("settings.disallowed-mobs");
        }
        for (String name : blockedNames) {
            try {
                EntityType type = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
                loaded.add(type);
            } catch (IllegalArgumentException ignored) {
            }
        }
        blockedMobs = loaded;

        ConfigurationSection guiSection = section("gui");
        if (guiSection != null) {
            guiItems.clear();
            for (String key : guiSection.getKeys(false)) {
                ConfigurationSection itemSection = guiSection.getConfigurationSection(key);
                if (itemSection == null) continue;
                String materialName = itemSection.getString("material");
                Material material = Material.matchMaterial(materialName);
                if (material == null) continue;
                int customModelData = itemSection.getInt("custom-model-data", 0);
                guiItems.put(key, new GuiItemDefinition(material, customModelData));
            }
        }
    }

    public boolean isAllowedMob(EntityType type) {
        return type != null && type.isAlive() && type.isSpawnable() && !blockedMobs.contains(type);
    }

    public FuelDefinition fuel(Material material) {
        return fuels.get(material);
    }

    public Map<Material, FuelDefinition> fuels() {
        return Map.copyOf(fuels);
    }

    public int maxLevel(UpgradeType type) {
        return maxLevels.getOrDefault(type, 0);
    }

    public double upgradeCost(UpgradeType type, int nextLevel) {
        return getDoubleListValue("upgrades." + type.key() + ".costs", nextLevel, Double.MAX_VALUE);
    }

    public int minDelay(int speedLevel) {
        return getIntListValue("upgrades.speed.min-delay", speedLevel, 200);
    }

    public int maxDelay(int speedLevel) {
        return getIntListValue("upgrades.speed.max-delay", speedLevel, 800);
    }

    public int spawnCount(int amountLevel) {
        return getIntListValue("upgrades.amount.spawn-count", amountLevel, 4);
    }

    public int playerRange(int rangeLevel) {
        return getIntListValue("upgrades.range.player-range", rangeLevel, 16);
    }

    public double dropMultiplier(int dropLevel) {
        return getDoubleListValue("upgrades.drops.multiplier", dropLevel, 1.0D);
    }

    public double xpMultiplier(int xpLevel) {
        return getDoubleListValue("upgrades.xp.multiplier", xpLevel, 1.0D);
    }

    public double fuelConsumptionMultiplier(int fuelLevel) {
        return getDoubleListValue("upgrades.fuel.consumption-multiplier", fuelLevel, 1.0D);
    }

    public long fuelConsumePerSpawn() {
        return Math.max(0L, longValue("fuel.consume-per-spawn", 20L));
    }

    public int internalStorageMaxStacks() {
        return Math.max(9, intValue("settings.internal-storage-max-stacks", 216));
    }

    public int maxLinkDistance() {
        return Math.max(1, intValue("settings.max-link-distance", 64));
    }

    public boolean hologramsEnabled() {
        return booleanValue("settings.holograms-enabled", true);
    }

    public boolean animationsEnabled() {
        return booleanValue("settings.animations-enabled", true);
    }

    public boolean economyEnabled() {
        return booleanValue("settings.economy.enabled", true);
    }

    public boolean requireVault() {
        return booleanValue("settings.economy.require-vault", true);
    }

    public boolean removeReturnsStoredItems() {
        return booleanValue("settings.remove-spawner-returns-stored-items", true);
    }

    public long saveIntervalSeconds() {
        return longValue("settings.save-interval-seconds", 120L);
    }

    public FallbackMode targetFullFallback() {
        return fallback("settings.fallback-when-target-full", FallbackMode.INTERNAL);
    }

    public FallbackMode targetMissingFallback() {
        return fallback("settings.fallback-when-target-missing", FallbackMode.INTERNAL);
    }

    public GuiItemDefinition getGuiItem(String key) {
        return guiItems.getOrDefault(key, new GuiItemDefinition(Material.STONE, 0));
    }

    private FallbackMode fallback(String path, FallbackMode def) {
        try {
            return FallbackMode.valueOf(stringValue(path, def.name()).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return def;
        }
    }

    private int getIntListValue(String path, int index, int def) {
        List<Integer> values = intList(path);
        if (values.isEmpty()) return def;
        return values.get(Math.min(Math.max(0, index), values.size() - 1));
    }

    private double getDoubleListValue(String path, int index, double def) {
        List<Double> values = doubleList(path);
        if (values.isEmpty()) return def;
        return values.get(Math.min(Math.max(0, index), values.size() - 1));
    }

    private ConfigurationSection section(String path) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        return section != null ? section : defaults.getConfigurationSection(path);
    }

    private int intValue(String path, int def) {
        return plugin.getConfig().contains(path) ? plugin.getConfig().getInt(path, def) : defaults.getInt(path, def);
    }

    private long longValue(String path, long def) {
        return plugin.getConfig().contains(path) ? plugin.getConfig().getLong(path, def) : defaults.getLong(path, def);
    }

    private double doubleValue(String path, double def) {
        return plugin.getConfig().contains(path) ? plugin.getConfig().getDouble(path, def) : defaults.getDouble(path, def);
    }

    private boolean booleanValue(String path, boolean def) {
        return plugin.getConfig().contains(path) ? plugin.getConfig().getBoolean(path, def) : defaults.getBoolean(path, def);
    }

    private String stringValue(String path, String def) {
        return plugin.getConfig().contains(path) ? plugin.getConfig().getString(path, def) : defaults.getString(path, def);
    }

    private List<String> stringList(String path) {
        List<String> values = plugin.getConfig().getStringList(path);
        return values.isEmpty() ? defaults.getStringList(path) : values;
    }

    private List<Integer> intList(String path) {
        List<Integer> values = plugin.getConfig().getIntegerList(path);
        return values.isEmpty() ? defaults.getIntegerList(path) : values;
    }

    private List<Double> doubleList(String path) {
        List<Double> values = plugin.getConfig().getDoubleList(path);
        return values.isEmpty() ? defaults.getDoubleList(path) : values;
    }

    public record FuelDefinition(long duration, double efficiency) {
        public long effectiveTicks() {
            return Math.max(0L, Math.round(duration * efficiency));
        }
    }
}
