package io.github.henrique02w.advancedcustomspawners.gui;

import java.util.UUID;

public final class SpawnerGuiHolder implements org.bukkit.inventory.InventoryHolder {
    private final UUID spawnerId;
    private org.bukkit.inventory.Inventory inventory;

    public SpawnerGuiHolder(UUID spawnerId) {
        this.spawnerId = spawnerId;
    }

    public UUID spawnerId() {
        return spawnerId;
    }

    @Override
    public org.bukkit.inventory.Inventory getInventory() {
        return inventory;
    }

    public void inventory(org.bukkit.inventory.Inventory inventory) {
        this.inventory = inventory;
    }
}
