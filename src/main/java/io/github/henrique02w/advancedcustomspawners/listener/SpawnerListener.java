package io.github.henrique02w.advancedcustomspawners.listener;

import io.github.henrique02w.advancedcustomspawners.config.PluginConfig;
import io.github.henrique02w.advancedcustomspawners.gui.GuiManager;
import io.github.henrique02w.advancedcustomspawners.model.BlockLocation;
import io.github.henrique02w.advancedcustomspawners.model.FallbackMode;
import io.github.henrique02w.advancedcustomspawners.model.SpawnerData;
import io.github.henrique02w.advancedcustomspawners.model.UpgradeType;
import io.github.henrique02w.advancedcustomspawners.service.ItemFactory;
import io.github.henrique02w.advancedcustomspawners.service.LinkService;
import io.github.henrique02w.advancedcustomspawners.service.MessageService;
import io.github.henrique02w.advancedcustomspawners.service.SpawnerManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SpawnerListener implements Listener {
    private final PluginConfig config;
    private final MessageService messages;
    private final ItemFactory itemFactory;
    private final SpawnerManager spawnerManager;
    private final LinkService linkService;
    private final GuiManager guiManager;

    public SpawnerListener(Plugin plugin, PluginConfig config, MessageService messages, ItemFactory itemFactory,
                           SpawnerManager spawnerManager, LinkService linkService, GuiManager guiManager) {
        this.config = config;
        this.messages = messages;
        this.itemFactory = itemFactory;
        this.spawnerManager = spawnerManager;
        this.linkService = linkService;
        this.guiManager = guiManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!itemFactory.isCustomSpawnerItem(item)) return;
        EntityType type = itemFactory.itemMob(item);
        if (type == null || !config.isAllowedMob(type)) {
            event.setCancelled(true);
            messages.send(event.getPlayer(), "invalid-mob");
            return;
        }
        SpawnerData data = spawnerManager.create(event.getBlockPlaced(), type, item);
        spawnerManager.applyToBlock(data);
        messages.send(event.getPlayer(), "spawner-placed");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        SpawnerData spawner = spawnerManager.byBlock(block).orElse(null);
        if (spawner != null) {
            event.setDropItems(false);
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), itemFactory.customSpawner(spawner, 1));
                List<ItemStack> cached = new ArrayList<>();
                cached.addAll(spawner.storage());
                cached.addAll(spawner.pendingRoute());
                for (ItemStack item : cached) {
                    block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.75, 0.5), item.clone());
                }
            }
            spawnerManager.remove(spawner);
            messages.send(event.getPlayer(), "spawner-broken");
            return;
        }

        BlockLocation broken = BlockLocation.fromBlock(block);
        for (SpawnerData data : spawnerManager.all()) {
            if (broken.equals(data.targetLocation())) {
                if (config.targetMissingFallback() == FallbackMode.DISABLE) {
                    spawnerManager.setTarget(data, null);
                } else {
                    data.targetLocation(null);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getClickedBlock() == null) return;
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();

        if (linkService.hasPending(player)) {
            event.setCancelled(true);
            linkService.complete(player, clicked);
            return;
        }

        SpawnerData data = spawnerManager.byBlock(clicked).orElse(null);
        if (data == null) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (itemFactory.isEmptyCaptureEgg(hand)) {
            return;
        }
        if (itemFactory.isCaptureEgg(hand)) {
            event.setCancelled(true);
            EntityType type = itemFactory.itemMob(hand);
            if (type == null || !config.isAllowedMob(type)) {
                messages.send(player, "invalid-mob");
                return;
            }
            spawnerManager.setType(data, type);
            hand.setAmount(hand.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.7f, 1.4f);
            messages.send(player, "type-changed", Map.of("mob", type.name()));
            return;
        }

        if (clicked.getType() == Material.SPAWNER && player.hasPermission("advancedspawners.use")) {
            event.setCancelled(true);
            guiManager.open(player, data);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!itemFactory.isEmptyCaptureEgg(hand)) return;
        if (!(event.getRightClicked() instanceof LivingEntity living) || living instanceof Player) return;

        EntityType type = living.getType();
        if (!config.isAllowedMob(type)) {
            messages.send(player, "invalid-mob");
            return;
        }

        event.setCancelled(true);
        if (player.getGameMode() == GameMode.CREATIVE) {
            player.getInventory().addItem(itemFactory.captureEgg(type, 1)).values()
                    .forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        } else {
            hand.setAmount(hand.getAmount() - 1);
            player.getInventory().addItem(itemFactory.captureEgg(type, 1)).values()
                    .forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
        living.remove();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
        messages.send(player, "mob-captured", Map.of("mob", type.name()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        SpawnerData data = spawnerManager.byBlock(event.getSpawner().getBlock()).orElse(null);
        if (data == null) return;
        spawnerManager.applyToBlock(data);
        if (!data.enabled() || data.upgrade(UpgradeType.ENVIRONMENT) > 0) {
            event.setCancelled(true);
            return;
        }
        if (!spawnerManager.consumeFuelForSpawn(data)) {
            event.setCancelled(true);
            return;
        }
        spawnerManager.tagSpawnedEntity(event.getEntity(), data);
        spawnerManager.animate(data);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        SpawnerData data = spawnerManager.fromSpawnedEntity(event.getEntity()).orElse(null);
        if (data == null) return;

        List<ItemStack> drops = spawnerManager.multiplyDrops(event.getDrops(), config.dropMultiplier(data.upgrade(UpgradeType.DROPS)));
        event.getDrops().clear();
        event.setDroppedExp((int) Math.round(event.getDroppedExp() * config.xpMultiplier(data.upgrade(UpgradeType.XP))));
        spawnerManager.handleDrops(data, drops);
    }
}
