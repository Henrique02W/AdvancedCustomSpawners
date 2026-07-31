package com.example.advancedspawners.model;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SpawnerData {
    private final UUID id;
    private BlockLocation location;
    private EntityType entityType;
    private final EnumMap<UpgradeType, Integer> upgrades = new EnumMap<>(UpgradeType.class);
    private long fuelTicks;
    private DropMode dropMode;
    private BlockLocation targetLocation;
    private boolean enabled = true;
    private final List<ItemStack> storage = new ArrayList<>();
    private final List<ItemStack> pendingRoute = new ArrayList<>();
    private boolean dirty = true;

    public SpawnerData(UUID id, BlockLocation location, EntityType entityType) {
        this.id = id;
        this.location = location;
        this.entityType = entityType;
        this.dropMode = DropMode.INTERNAL;
        for (UpgradeType type : UpgradeType.values()) {
            upgrades.put(type, 0);
        }
    }

    public UUID id() {
        return id;
    }

    public BlockLocation location() {
        return location;
    }

    public void location(BlockLocation location) {
        this.location = location;
        markDirty();
    }

    public EntityType entityType() {
        return entityType;
    }

    public void entityType(EntityType entityType) {
        this.entityType = entityType;
        markDirty();
    }

    public int upgrade(UpgradeType type) {
        return upgrades.getOrDefault(type, 0);
    }

    public void upgrade(UpgradeType type, int level) {
        upgrades.put(type, Math.max(0, level));
        markDirty();
    }

    public Map<UpgradeType, Integer> upgrades() {
        return upgrades;
    }

    public long fuelTicks() {
        return fuelTicks;
    }

    public void addFuel(long ticks) {
        fuelTicks = Math.max(0L, fuelTicks + ticks);
        markDirty();
    }

    public boolean consumeFuel(long ticks) {
        if (ticks <= 0) return true;
        if (fuelTicks < ticks) return false;
        fuelTicks -= ticks;
        markDirty();
        return true;
    }

    public DropMode dropMode() {
        return dropMode;
    }

    public void dropMode(DropMode dropMode) {
        this.dropMode = dropMode;
        markDirty();
    }

    public BlockLocation targetLocation() {
        return targetLocation;
    }

    public boolean enabled() {
        return enabled;
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
        markDirty();
    }

    public void targetLocation(BlockLocation targetLocation) {
        this.targetLocation = targetLocation;
        this.dropMode = targetLocation == null ? DropMode.INTERNAL : DropMode.ROUTED;
        markDirty();
    }

    public List<ItemStack> storage() {
        return storage;
    }

    public List<ItemStack> pendingRoute() {
        return pendingRoute;
    }

    public int storedItemCount() {
        return storage.stream().filter(i -> i != null && !i.getType().isAir()).mapToInt(ItemStack::getAmount).sum();
    }

    public boolean dirty() {
        return dirty;
    }

    public void markDirty() {
        dirty = true;
    }

    public void markClean() {
        dirty = false;
    }
}
