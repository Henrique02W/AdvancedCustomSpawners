package io.github.henrique02w.advancedcustomspawners.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class BlockLocation {
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public BlockLocation(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockLocation fromBlock(Block block) {
        return new BlockLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public static BlockLocation fromSection(ConfigurationSection section) {
        if (section == null) return null;
        String world = section.getString("world");
        if (world == null || world.isBlank()) return null;
        return new BlockLocation(world, section.getInt("x"), section.getInt("y"), section.getInt("z"));
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", world);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        return map;
    }

    public Location toLocation() {
        World bukkitWorld = Bukkit.getWorld(world);
        return bukkitWorld == null ? null : new Location(bukkitWorld, x, y, z);
    }

    public Block getBlockIfLoaded() {
        World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null || !bukkitWorld.isChunkLoaded(x >> 4, z >> 4)) return null;
        return bukkitWorld.getBlockAt(x, y, z);
    }

    public double distanceSquared(BlockLocation other) {
        if (other == null || !world.equals(other.world)) return Double.MAX_VALUE;
        long dx = x - other.x;
        long dy = y - other.y;
        long dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public String compact() {
        return world + ":" + x + "," + y + "," + z;
    }

    public String world() {
        return world;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockLocation that)) return false;
        return x == that.x && y == that.y && z == that.z && world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }
}
