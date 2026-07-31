package com.example.advancedspawners.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class Keys {
    public final NamespacedKey spawnerId;
    public final NamespacedKey itemType;
    public final NamespacedKey mobType;
    public final NamespacedKey spawnerPayload;
    public final NamespacedKey linkedSpawner;

    public Keys(Plugin plugin) {
        this.spawnerId = new NamespacedKey(plugin, "spawner_id");
        this.itemType = new NamespacedKey(plugin, "item_type");
        this.mobType = new NamespacedKey(plugin, "mob_type");
        this.spawnerPayload = new NamespacedKey(plugin, "spawner_payload");
        this.linkedSpawner = new NamespacedKey(plugin, "linked_spawner_id");
    }
}
