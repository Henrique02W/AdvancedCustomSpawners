package io.github.henrique02w.advancedcustomspawners.gui;

import io.github.henrique02w.advancedcustomspawners.config.PluginConfig;
import io.github.henrique02w.advancedcustomspawners.config.PluginConfig.FuelDefinition;
import io.github.henrique02w.advancedcustomspawners.model.SpawnerData;
import io.github.henrique02w.advancedcustomspawners.model.UpgradeType;
import io.github.henrique02w.advancedcustomspawners.service.EconomyService;
import io.github.henrique02w.advancedcustomspawners.service.LinkService;
import io.github.henrique02w.advancedcustomspawners.service.MessageService;
import io.github.henrique02w.advancedcustomspawners.service.SpawnerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GuiListener implements Listener {
    private final GuiManager guiManager;
    private final PluginConfig config;
    private final MessageService messages;
    private final SpawnerManager spawnerManager;
    private final LinkService linkService;
    private final EconomyService economyService;
    private final Map<UUID, Long> removeConfirm = new HashMap<>();

    public GuiListener(GuiManager guiManager, PluginConfig config, MessageService messages, SpawnerManager spawnerManager, LinkService linkService, EconomyService economyService) {
        this.guiManager = guiManager;
        this.config = config;
        this.messages = messages;
        this.spawnerManager = spawnerManager;
        this.linkService = linkService;
        this.economyService = economyService;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof SpawnerGuiHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        SpawnerData data = guiManager.dataFrom(top).orElse(null);
        if (data == null) {
            player.closeInventory();
            return;
        }

        if (event.getClickedInventory() == player.getInventory()) {
            addFuelFromClickedItem(player, event.getCurrentItem(), data, top);
            return;
        }

        int slot = event.getRawSlot();
        UpgradeType clickedUpgrade = upgradeBySlot(slot);
        if (clickedUpgrade != null) {
            upgrade(player, data, clickedUpgrade, top);
            return;
        }
        if (slot == GuiManager.COLLECT_SLOT) {
            spawnerManager.withdrawAll(data, player.getInventory());
            guiManager.render(top, data);
            return;
        }
        if (slot == GuiManager.TOGGLE_SLOT) {
            data.enabled(!data.enabled());
            spawnerManager.applyToBlock(data);
            messages.send(player, data.enabled() ? "spawner-enabled" : "spawner-disabled");
            guiManager.render(top, data);
            return;
        }
        if (slot == GuiManager.LINK_SLOT) {
            player.closeInventory();
            linkService.start(player, data);
            return;
        }
        if (slot == GuiManager.UNLINK_SLOT) {
            spawnerManager.setTarget(data, null);
            messages.send(player, "unlink-success");
            guiManager.render(top, data);
            return;
        }
        if (slot == GuiManager.REMOVE_SLOT) {
            removeSpawner(player, data);
        }
    }

    private void addFuelFromClickedItem(Player player, ItemStack item, SpawnerData data, Inventory top) {
        if (item == null || item.getType().isAir()) return;
        FuelDefinition fuel = config.fuel(item.getType());
        if (fuel == null) return;
        item.setAmount(item.getAmount() - 1);
        data.addFuel(fuel.effectiveTicks());
        messages.send(player, "fuel-added", Map.of("ticks", String.valueOf(fuel.effectiveTicks())));
        guiManager.render(top, data);
    }

    private void upgrade(Player player, SpawnerData data, UpgradeType type, Inventory top) {
        int level = data.upgrade(type);
        int max = config.maxLevel(type);
        if (level >= max) {
            messages.send(player, "upgrade-max");
            return;
        }
        int next = level + 1;
        double cost = config.upgradeCost(type, next);
        if (config.economyEnabled() && config.requireVault() && !economyService.available()) {
            messages.send(player, "vault-missing");
            return;
        }
        if (!guiManager.takeUpgradeCost(player, cost)) {
            String amount = config.economyEnabled() && economyService.available() ? economyService.format(cost) : String.valueOf((int) Math.ceil(cost));
            messages.send(player, config.economyEnabled() ? "money-missing" : "upgrade-cost-missing", Map.of("amount", amount));
            return;
        }
        data.upgrade(type, next);
        spawnerManager.applyToBlock(data);
        messages.send(player, "upgrade-success", Map.of("upgrade", type.display(), "level", String.valueOf(next)));
        guiManager.render(top, data);
    }

    private UpgradeType upgradeBySlot(int slot) {
        return switch (slot) {
            case 10 -> UpgradeType.SPEED;
            case 11 -> UpgradeType.AMOUNT;
            case 12 -> UpgradeType.RANGE;
            case 13 -> UpgradeType.ENVIRONMENT;
            case 14 -> UpgradeType.DROPS;
            case 15 -> UpgradeType.XP;
            case 16 -> UpgradeType.FUEL;
            default -> null;
        };
    }

    private void removeSpawner(Player player, SpawnerData data) {
        if (!player.hasPermission("advancedspawners.remove")) {
            messages.send(player, "no-permission");
            return;
        }
        long now = System.currentTimeMillis();
        long last = removeConfirm.getOrDefault(player.getUniqueId(), 0L);
        if (now - last > 4000L) {
            removeConfirm.put(player.getUniqueId(), now);
            messages.send(player, "remove-confirm");
            return;
        }
        removeConfirm.remove(player.getUniqueId());
        player.closeInventory();
        spawnerManager.dismantleToPlayer(data, player, config.removeReturnsStoredItems());
        messages.send(player, "spawner-removed-gui");
    }
}
