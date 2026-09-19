package io.github.henrique02w.advancedcustomspawners.service;

import io.github.henrique02w.advancedcustomspawners.config.PluginConfig;
import io.github.henrique02w.advancedcustomspawners.hologram.HologramManager;
import io.github.henrique02w.advancedcustomspawners.model.BlockLocation;
import io.github.henrique02w.advancedcustomspawners.model.DropMode;
import io.github.henrique02w.advancedcustomspawners.model.FallbackMode;
import io.github.henrique02w.advancedcustomspawners.model.SpawnerData;
import io.github.henrique02w.advancedcustomspawners.model.UpgradeType;
import io.github.henrique02w.advancedcustomspawners.storage.SpawnerStorage;
import io.github.henrique02w.advancedcustomspawners.util.Keys;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class SpawnerManager {
    private final Plugin plugin;
    private final PluginConfig config;
    private final SpawnerStorage storage;
    private final ItemFactory itemFactory;
    private final HologramManager holograms;
    private final Keys keys;
    private final Map<UUID, SpawnerData> byId = new HashMap<>();
    private final Map<BlockLocation, UUID> byLocation = new HashMap<>();
    private final Map<UUID, Long> nextForcedSpawn = new HashMap<>();

    public SpawnerManager(Plugin plugin, PluginConfig config, SpawnerStorage storage, ItemFactory itemFactory, HologramManager holograms) {
        this.plugin = plugin;
        this.config = config;
        this.storage = storage;
        this.itemFactory = itemFactory;
        this.holograms = holograms;
        this.keys = itemFactory.keys();
    }

    public void load() {
        byId.clear();
        byLocation.clear();
        for (SpawnerData data : storage.loadAll()) {
            byId.put(data.id(), data);
            byLocation.put(data.location(), data.id());
            applyToBlock(data);
        }
    }

    public void save(boolean force) {
        storage.saveAll(byId.values(), force);
    }

    public Collection<SpawnerData> all() {
        return byId.values();
    }

    public Optional<SpawnerData> byId(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<SpawnerData> byBlock(Block block) {
        if (block == null || block.getType() != Material.SPAWNER) return Optional.empty();
        BlockState state = block.getState();
        if (state instanceof CreatureSpawner spawner) {
            String id = spawner.getPersistentDataContainer().get(keys.spawnerId, PersistentDataType.STRING);
            if (id != null) {
                try {
                    return Optional.ofNullable(byId.get(UUID.fromString(id)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return Optional.ofNullable(byId.get(byLocation.get(BlockLocation.fromBlock(block))));
    }

    public SpawnerData create(Block block, EntityType type) {
        SpawnerData data = new SpawnerData(UUID.randomUUID(), BlockLocation.fromBlock(block), type);
        byId.put(data.id(), data);
        byLocation.put(data.location(), data.id());
        applyToBlock(data);
        holograms.refresh(data);
        return data;
    }

    public SpawnerData create(Block block, EntityType type, ItemStack sourceItem) {
        SpawnerData data = new SpawnerData(UUID.randomUUID(), BlockLocation.fromBlock(block), type);
        itemFactory.applySpawnerItemData(sourceItem, data);
        byId.put(data.id(), data);
        byLocation.put(data.location(), data.id());
        applyToBlock(data);
        holograms.refresh(data);
        return data;
    }

    public void remove(SpawnerData data) {
        byId.remove(data.id());
        byLocation.remove(data.location());
        holograms.remove(data.id());
        storage.delete(data.id());
    }

    public void dismantleToPlayer(SpawnerData data, Player player, boolean includeStoredItems) {
        Block block = data.location().getBlockIfLoaded();
        if (block != null && block.getType() == Material.SPAWNER) {
            block.setType(Material.AIR, false);
        }
        giveOrDrop(player, itemFactory.customSpawner(data, 1));
        if (includeStoredItems) {
            List<ItemStack> cached = new ArrayList<>();
            cached.addAll(data.storage());
            cached.addAll(data.pendingRoute());
            for (ItemStack item : cached) {
                giveOrDrop(player, item.clone());
            }
        }
        remove(data);
    }

    public void applyRuntimeSettings() {
        for (SpawnerData data : all()) {
            applyToBlock(data);
        }
    }

    public void applyToBlock(SpawnerData data) {
        Block block = data.location().getBlockIfLoaded();
        if (block == null || block.getType() != Material.SPAWNER) return;
        BlockState state = block.getState();
        if (!(state instanceof CreatureSpawner spawner)) return;

        spawner.getPersistentDataContainer().set(keys.spawnerId, PersistentDataType.STRING, data.id().toString());
        spawner.setSpawnedType(data.entityType());
        int speed = data.upgrade(UpgradeType.SPEED);
        int min = config.minDelay(speed);
        int max = Math.max(min + 1, config.maxDelay(speed));
        spawner.setMinSpawnDelay(min);
        spawner.setMaxSpawnDelay(max);
        spawner.setSpawnCount(config.spawnCount(data.upgrade(UpgradeType.AMOUNT)));
        spawner.setRequiredPlayerRange(data.enabled() ? config.playerRange(data.upgrade(UpgradeType.RANGE)) : 0);
        spawner.update(true, false);
    }

    public void tickForcedEnvironmentSpawners() {
        long now = System.currentTimeMillis();
        for (SpawnerData data : all()) {
            if (!data.enabled() || data.upgrade(UpgradeType.ENVIRONMENT) <= 0) continue;
            if (nextForcedSpawn.getOrDefault(data.id(), 0L) > now) continue;
            Block block = data.location().getBlockIfLoaded();
            if (block == null || block.getType() != Material.SPAWNER) continue;
            if (!hasPlayerInRange(data)) continue;

            int delayTicks = ThreadLocalRandom.current().nextInt(
                    Math.max(1, config.minDelay(data.upgrade(UpgradeType.SPEED))),
                    Math.max(config.minDelay(data.upgrade(UpgradeType.SPEED)) + 1, config.maxDelay(data.upgrade(UpgradeType.SPEED)) + 1)
            );
            nextForcedSpawn.put(data.id(), now + delayTicks * 50L);
            if (!consumeFuelForSpawn(data)) continue;

            int count = config.spawnCount(data.upgrade(UpgradeType.AMOUNT));
            for (int i = 0; i < count; i++) {
                spawnIgnoringEnvironment(data);
            }
            animate(data);
        }
    }

    public boolean consumeFuelForSpawn(SpawnerData data) {
        int fuelLevel = data.upgrade(UpgradeType.FUEL);
        if (fuelLevel >= config.maxLevel(UpgradeType.FUEL) && config.fuelConsumptionMultiplier(fuelLevel) <= 0D) {
            return true;
        }
        long amount = Math.round(config.fuelConsumePerSpawn() * config.fuelConsumptionMultiplier(fuelLevel));
        return data.consumeFuel(amount);
    }

    public void tagSpawnedEntity(Entity entity, SpawnerData data) {
        entity.getPersistentDataContainer().set(keys.linkedSpawner, PersistentDataType.STRING, data.id().toString());
    }

    public Optional<SpawnerData> fromSpawnedEntity(Entity entity) {
        String id = entity.getPersistentDataContainer().get(keys.linkedSpawner, PersistentDataType.STRING);
        if (id == null) return Optional.empty();
        try {
            return Optional.ofNullable(byId.get(UUID.fromString(id)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public List<ItemStack> multiplyDrops(List<ItemStack> drops, double multiplier) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (drop == null || drop.getType().isAir() || drop.getAmount() <= 0) continue;
            double exact = drop.getAmount() * multiplier;
            int amount = (int) Math.floor(exact);
            if (ThreadLocalRandom.current().nextDouble() < exact - amount) amount++;
            while (amount > 0) {
                ItemStack copy = drop.clone();
                copy.setAmount(Math.min(copy.getMaxStackSize(), amount));
                result.add(copy);
                amount -= copy.getAmount();
            }
        }
        return result;
    }

    public void handleDrops(SpawnerData data, List<ItemStack> drops) {
        if (drops.isEmpty()) return;
        if (data.dropMode() == DropMode.ROUTED && data.targetLocation() != null) {
            routeOrFallback(data, drops, false);
        } else {
            storeInternal(data, drops);
        }
        holograms.refresh(data);
    }

    public void retryPendingRoutes() {
        for (SpawnerData data : all()) {
            if (data.pendingRoute().isEmpty()) continue;
            List<ItemStack> copy = new ArrayList<>(data.pendingRoute());
            data.pendingRoute().clear();
            routeOrFallback(data, copy, true);
        }
    }

    public void routeOrFallback(SpawnerData data, List<ItemStack> drops, boolean retry) {
        Block target = data.targetLocation() == null ? null : data.targetLocation().getBlockIfLoaded();
        if (target == null) {
            if (retry || config.targetMissingFallback() == FallbackMode.PAUSE) {
                data.pendingRoute().addAll(cloneItems(drops));
            } else if (config.targetMissingFallback() == FallbackMode.DISABLE) {
                data.targetLocation(null);
                storeInternal(data, drops);
            } else {
                storeInternal(data, drops);
            }
            data.markDirty();
            return;
        }

        BlockState state = target.getState();
        if (!(state instanceof Container container)) {
            if (config.targetMissingFallback() == FallbackMode.DISABLE) {
                data.targetLocation(null);
            }
            storeInternal(data, drops);
            return;
        }

        Map<Integer, ItemStack> leftovers = container.getInventory().addItem(cloneItems(drops).toArray(ItemStack[]::new));
        if (leftovers.isEmpty()) {
            data.markDirty();
            return;
        }

        List<ItemStack> leftoverItems = new ArrayList<>(leftovers.values());
        FallbackMode fallback = config.targetFullFallback();
        if (fallback == FallbackMode.PAUSE) {
            data.pendingRoute().addAll(leftoverItems);
        } else if (fallback == FallbackMode.DROP) {
            dropAtSpawner(data, leftoverItems);
        } else {
            storeInternal(data, leftoverItems);
        }
        data.markDirty();
    }

    public boolean storeInternal(SpawnerData data, List<ItemStack> items) {
        int maxStacks = config.internalStorageMaxStacks();
        List<ItemStack> overflow = mergeInto(data.storage(), cloneItems(items), maxStacks);
        if (!overflow.isEmpty()) {
            dropAtSpawner(data, overflow);
        }
        data.markDirty();
        return overflow.isEmpty();
    }

    public List<ItemStack> withdrawAll(SpawnerData data, Inventory target) {
        List<ItemStack> moving = new ArrayList<>(data.storage());
        data.storage().clear();
        Map<Integer, ItemStack> leftovers = target.addItem(moving.toArray(ItemStack[]::new));
        if (!leftovers.isEmpty()) {
            data.storage().addAll(leftovers.values());
        }
        data.markDirty();
        holograms.refresh(data);
        return new ArrayList<>(leftovers.values());
    }

    public void setTarget(SpawnerData data, BlockLocation target) {
        data.targetLocation(target);
        holograms.refresh(data);
    }

    public void setType(SpawnerData data, EntityType type) {
        data.entityType(type);
        applyToBlock(data);
        holograms.refresh(data);
    }

    private List<ItemStack> mergeInto(List<ItemStack> storage, List<ItemStack> incoming, int maxStacks) {
        List<ItemStack> overflow = new ArrayList<>();
        for (ItemStack item : incoming) {
            int remaining = item.getAmount();
            for (ItemStack stored : storage) {
                if (remaining <= 0) break;
                if (stored != null && stored.isSimilar(item) && stored.getAmount() < stored.getMaxStackSize()) {
                    int move = Math.min(stored.getMaxStackSize() - stored.getAmount(), remaining);
                    stored.setAmount(stored.getAmount() + move);
                    remaining -= move;
                }
            }
            while (remaining > 0 && storage.size() < maxStacks) {
                ItemStack copy = item.clone();
                copy.setAmount(Math.min(copy.getMaxStackSize(), remaining));
                storage.add(copy);
                remaining -= copy.getAmount();
            }
            while (remaining > 0) {
                ItemStack copy = item.clone();
                copy.setAmount(Math.min(copy.getMaxStackSize(), remaining));
                overflow.add(copy);
                remaining -= copy.getAmount();
            }
        }
        return overflow;
    }

    private void dropAtSpawner(SpawnerData data, List<ItemStack> items) {
        Location location = data.location().toLocation();
        if (location == null) return;
        World world = location.getWorld();
        Location dropLocation = location.add(0.5, 1.0, 0.5);
        for (ItemStack item : items) {
            world.dropItemNaturally(dropLocation, item.clone());
        }
    }

    private List<ItemStack> cloneItems(List<ItemStack> items) {
        return items.stream()
                .filter(item -> item != null && !item.getType().isAir() && item.getAmount() > 0)
                .map(ItemStack::clone)
                .toList();
    }

    private void giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    public void animate(SpawnerData data) {
        if (!config.animationsEnabled()) return;
        Location location = data.location().toLocation();
        if (location != null && location.getWorld() != null) {
            location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location.add(0.5, 1.2, 0.5), 8, 0.25, 0.25, 0.25, 0.01);
        }
    }

    private boolean hasPlayerInRange(SpawnerData data) {
        Location location = data.location().toLocation();
        if (location == null || location.getWorld() == null) return false;
        double rangeSquared = Math.pow(config.playerRange(data.upgrade(UpgradeType.RANGE)), 2);
        return location.getWorld().getPlayers().stream()
                .anyMatch(player -> player.getLocation().distanceSquared(location) <= rangeSquared);
    }

    private void spawnIgnoringEnvironment(SpawnerData data) {
        Location base = data.location().toLocation();
        if (base == null || base.getWorld() == null) return;
        World world = base.getWorld();
        int radius = Math.max(1, 4 + data.upgrade(UpgradeType.RANGE));
        double x = base.getX() + 0.5 + ThreadLocalRandom.current().nextDouble(-radius, radius + 1);
        double y = base.getY() + 1.0;
        double z = base.getZ() + 0.5 + ThreadLocalRandom.current().nextDouble(-radius, radius + 1);
        Location spawnAt = new Location(world, x, y, z);
        if (!world.isChunkLoaded(spawnAt.getBlockX() >> 4, spawnAt.getBlockZ() >> 4)) return;

        for (int i = 0; i < 3 && spawnAt.getBlock().getType().isSolid(); i++) {
            spawnAt.add(0, 1, 0);
        }

        try {
            Class<? extends Entity> entityClass = data.entityType().getEntityClass();
            Entity entity = entityClass == null
                    ? world.spawnEntity(spawnAt, data.entityType())
                    : world.spawn(spawnAt, entityClass, CreatureSpawnEvent.SpawnReason.SPAWNER, spawned -> {
                        tagSpawnedEntity(spawned, data);
                        if (spawned instanceof LivingEntity living) {
                            living.setRemainingAir(living.getMaximumAir());
                        }
                    });
            tagSpawnedEntity(entity, data);
        } catch (RuntimeException ignored) {
        }
    }
}
