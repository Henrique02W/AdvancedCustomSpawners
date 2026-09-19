package io.github.henrique02w.advancedcustomspawners.service;

import io.github.henrique02w.advancedcustomspawners.config.PluginConfig;
import io.github.henrique02w.advancedcustomspawners.model.BlockLocation;
import io.github.henrique02w.advancedcustomspawners.model.SpawnerData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class LinkService {
    private final PluginConfig config;
    private final MessageService messages;
    private final SpawnerManager spawnerManager;
    private final Map<UUID, UUID> pending = new HashMap<>();

    public LinkService(PluginConfig config, MessageService messages, SpawnerManager spawnerManager) {
        this.config = config;
        this.messages = messages;
        this.spawnerManager = spawnerManager;
    }

    public void start(Player player, SpawnerData data) {
        pending.put(player.getUniqueId(), data.id());
        messages.send(player, "link-start");
    }

    public boolean hasPending(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    public void cancel(Player player) {
        pending.remove(player.getUniqueId());
        messages.send(player, "link-cancelled");
    }

    public boolean complete(Player player, Block target) {
        UUID spawnerId = pending.remove(player.getUniqueId());
        if (spawnerId == null) return false;
        Optional<SpawnerData> optional = spawnerManager.byId(spawnerId);
        if (optional.isEmpty()) return true;
        SpawnerData data = optional.get();
        if (target.getType() == Material.SPAWNER) {
            messages.send(player, "link-invalid");
            return true;
        }
        if (!(target.getState() instanceof Container)) {
            messages.send(player, "link-invalid");
            return true;
        }
        BlockLocation targetLocation = BlockLocation.fromBlock(target);
        int max = config.maxLinkDistance();
        if (data.location().distanceSquared(targetLocation) > (double) max * max) {
            messages.send(player, "link-too-far");
            return true;
        }
        spawnerManager.setTarget(data, targetLocation);
        messages.send(player, "link-success");
        return true;
    }
}
