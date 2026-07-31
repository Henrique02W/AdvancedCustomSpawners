package com.example.advancedspawners.service;

import com.example.advancedspawners.config.PluginConfig;
import com.example.advancedspawners.model.SpawnerData;
import com.example.advancedspawners.model.UpgradeType;
import com.example.advancedspawners.util.Keys;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class ItemFactory {
    private final Plugin plugin;
    private final PluginConfig config;
    private final Keys keys;
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    public ItemFactory(Plugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.keys = new Keys(plugin);
    }

    public Keys keys() {
        return keys;
    }

    public ItemStack customSpawner(EntityType type, int amount) {
        return customSpawner(type, amount, Map.of(), 0L, true);
    }

    public ItemStack customSpawner(SpawnerData data, int amount) {
        Map<UpgradeType, Integer> upgrades = data.upgrades().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return customSpawner(data.entityType(), amount, upgrades, data.fuelTicks(), data.enabled());
    }

    private ItemStack customSpawner(EntityType type, int amount, Map<UpgradeType, Integer> upgrades, long fuelTicks, boolean enabled) {
        ItemStack item = new ItemStack(Material.SPAWNER, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(legacy.deserialize(apply(plugin.getConfig().getString("items.spawner.name", "&bSpawner Customizado"), type, upgrades, fuelTicks, enabled)));
        meta.lore(plugin.getConfig().getStringList("items.spawner.lore").stream()
                .map(line -> legacy.deserialize(apply(line, type, upgrades, fuelTicks, enabled)))
                .toList());
        meta.getPersistentDataContainer().set(keys.itemType, PersistentDataType.STRING, "CUSTOM_SPAWNER");
        meta.getPersistentDataContainer().set(keys.mobType, PersistentDataType.STRING, type.name());
        meta.getPersistentDataContainer().set(keys.spawnerPayload, PersistentDataType.STRING, serializePayload(upgrades, fuelTicks, enabled));
        if (meta instanceof BlockStateMeta blockStateMeta && blockStateMeta.getBlockState() instanceof CreatureSpawner spawner) {
            spawner.setSpawnedType(type);
            blockStateMeta.setBlockState(spawner);
        }
        item.setItemMeta(meta);
        return item;
    }

    public void applySpawnerItemData(ItemStack item, SpawnerData data) {
        if (item == null || !item.hasItemMeta()) return;
        String payload = item.getItemMeta().getPersistentDataContainer().get(keys.spawnerPayload, PersistentDataType.STRING);
        if (payload == null || payload.isBlank()) return;
        for (String part : payload.split(";")) {
            String[] split = part.split("=", 2);
            if (split.length != 2) continue;
            if (split[0].equalsIgnoreCase("fuel")) {
                try {
                    data.addFuel(Long.parseLong(split[1]));
                } catch (NumberFormatException ignored) {
                }
                continue;
            }
            if (split[0].equalsIgnoreCase("enabled")) {
                data.enabled(Boolean.parseBoolean(split[1]));
                continue;
            }
            try {
                UpgradeType upgrade = UpgradeType.valueOf(split[0].toUpperCase(Locale.ROOT));
                data.upgrade(upgrade, Math.max(0, Integer.parseInt(split[1])));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public ItemStack captureEgg(EntityType type, int amount) {
        ItemStack item = new ItemStack(Material.ECHO_SHARD, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(legacy.deserialize(apply(plugin.getConfig().getString("items.capture-egg.name", "&eCapturador"), type, Map.of(), 0L, true)));
        meta.lore(plugin.getConfig().getStringList("items.capture-egg.lore").stream()
                .map(line -> legacy.deserialize(apply(line, type, Map.of(), 0L, true)))
                .toList());
        meta.getPersistentDataContainer().set(keys.itemType, PersistentDataType.STRING, "CAPTURE_EGG");
        meta.getPersistentDataContainer().set(keys.mobType, PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack emptyCaptureEgg(int amount) {
        ItemStack item = new ItemStack(Material.ECHO_SHARD, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(legacy.deserialize(plugin.getConfig().getString("items.empty-capture-egg.name", "&eCapturador de Mob &7(Vazio)")));
        meta.lore(plugin.getConfig().getStringList("items.empty-capture-egg.lore").stream()
                .map(legacy::deserialize)
                .toList());
        meta.getPersistentDataContainer().set(keys.itemType, PersistentDataType.STRING, "CAPTURE_EGG_EMPTY");
        item.setItemMeta(meta);
        return item;
    }

    public EntityType itemMob(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return null;
        String value = item.getItemMeta().getPersistentDataContainer().get(keys.mobType, PersistentDataType.STRING);
        if (value == null) return null;
        try {
            EntityType type = EntityType.valueOf(value.toUpperCase(Locale.ROOT));
            return config.isAllowedMob(type) ? type : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean isCustomSpawnerItem(ItemStack item) {
        return isItemType(item, "CUSTOM_SPAWNER");
    }

    public boolean isCaptureEgg(ItemStack item) {
        return isItemType(item, "CAPTURE_EGG");
    }

    public boolean isEmptyCaptureEgg(ItemStack item) {
        return isItemType(item, "CAPTURE_EGG_EMPTY");
    }

    public ItemStack button(Material material, String name, List<String> lore) {
        return button(material, name, lore, 0);
    }

    public ItemStack button(Material material, String name, List<String> lore, int customModelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(legacy.deserialize(name));
        meta.lore(lore.stream().map(legacy::deserialize).toList());
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);
        return item;
    }

    private boolean isItemType(ItemStack item, String expected) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        String value = item.getItemMeta().getPersistentDataContainer().get(keys.itemType, PersistentDataType.STRING);
        return expected.equals(value);
    }

    private String apply(String line, EntityType type, Map<UpgradeType, Integer> upgrades, long fuelTicks, boolean enabled) {
        String result = line.replace("{mob}", type.name())
                .replace("{fuel_ticks}", String.valueOf(fuelTicks))
                .replace("{enabled}", enabled ? "Ligado" : "Desligado");
        for (UpgradeType upgrade : UpgradeType.values()) {
            result = result.replace("{" + upgrade.key() + "_level}", String.valueOf(upgrades.getOrDefault(upgrade, 0)));
        }
        return result;
    }

    private String serializePayload(Map<UpgradeType, Integer> upgrades, long fuelTicks, boolean enabled) {
        StringBuilder builder = new StringBuilder("fuel=").append(Math.max(0L, fuelTicks))
                .append(";enabled=").append(enabled);
        for (UpgradeType upgrade : UpgradeType.values()) {
            builder.append(';').append(upgrade.name()).append('=').append(upgrades.getOrDefault(upgrade, 0));
        }
        return builder.toString();
    }
}
