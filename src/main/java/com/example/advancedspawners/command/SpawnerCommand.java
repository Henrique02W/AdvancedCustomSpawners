package com.example.advancedspawners.command;

import com.example.advancedspawners.AdvancedCustomSpawnersPlugin;
import com.example.advancedspawners.config.PluginConfig;
import com.example.advancedspawners.model.SpawnerData;
import com.example.advancedspawners.service.ItemFactory;
import com.example.advancedspawners.service.LinkService;
import com.example.advancedspawners.service.MessageService;
import com.example.advancedspawners.service.SpawnerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SpawnerCommand implements CommandExecutor, TabCompleter {
    private final AdvancedCustomSpawnersPlugin plugin;
    private final PluginConfig config;
    private final MessageService messages;
    private final ItemFactory itemFactory;
    private final SpawnerManager spawnerManager;
    private final LinkService linkService;

    public SpawnerCommand(AdvancedCustomSpawnersPlugin plugin, PluginConfig config, MessageService messages,
                          ItemFactory itemFactory, SpawnerManager spawnerManager, LinkService linkService) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.itemFactory = itemFactory;
        this.spawnerManager = spawnerManager;
        this.linkService = linkService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            messages.send(sender, "invalid-command");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give" -> give(sender, args);
            case "giveempty", "givevazio" -> giveEmpty(sender, args);
            case "reload" -> reload(sender);
            case "info" -> info(sender);
            case "link" -> link(sender);
            case "unlink" -> unlink(sender);
            case "remove", "remover" -> remove(sender);
            default -> messages.send(sender, "invalid-command");
        }
        return true;
    }

    private void giveEmpty(CommandSender sender, String[] args) {
        if (!sender.hasPermission("advancedspawners.give")) {
            messages.send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("/spawner giveempty <player> [amount]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            messages.send(sender, "player-not-found");
            return;
        }
        int amount = args.length >= 3 ? parsePositive(args[2], 1) : 1;
        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(itemFactory.emptyCaptureEgg(amount));
        for (ItemStack leftover : leftovers.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), leftover);
        }
        messages.send(sender, "give-empty-egg", Map.of("player", target.getName()));
    }

    private void give(CommandSender sender, String[] args) {
        if (!sender.hasPermission("advancedspawners.give")) {
            messages.send(sender, "no-permission");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("/spawner give <player> <mob> [amount] [egg]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            messages.send(sender, "player-not-found");
            return;
        }
        EntityType type = parseType(args[2]);
        if (type == null || !config.isAllowedMob(type)) {
            messages.send(sender, "invalid-mob");
            return;
        }
        int amount = args.length >= 4 ? parsePositive(args[3], 1) : 1;
        boolean egg = args.length >= 5 && args[4].equalsIgnoreCase("egg");
        ItemStack item = egg ? itemFactory.captureEgg(type, amount) : itemFactory.customSpawner(type, amount);
        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), leftover);
        }
        messages.send(sender, egg ? "give-egg" : "give-spawner", Map.of("mob", type.name(), "player", target.getName()));
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("advancedspawners.reload")) {
            messages.send(sender, "no-permission");
            return;
        }
        plugin.reloadEverything();
        messages.send(sender, "reload");
    }

    private void info(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "only-player");
            return;
        }
        if (!player.hasPermission("advancedspawners.info")) {
            messages.send(player, "no-permission");
            return;
        }
        SpawnerData data = targetedSpawner(player);
        if (data == null) {
            messages.send(player, "not-custom-spawner");
            return;
        }
        messages.sendList(player, "info", Map.of(
                "mob", data.entityType().name(),
                "fuel", String.valueOf(data.fuelTicks()),
                "target", data.targetLocation() == null ? "desconectado" : data.targetLocation().compact(),
                "items", String.valueOf(data.storedItemCount())
        ));
    }

    private void link(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "only-player");
            return;
        }
        if (!player.hasPermission("advancedspawners.link")) {
            messages.send(player, "no-permission");
            return;
        }
        SpawnerData data = targetedSpawner(player);
        if (data == null) {
            messages.send(player, "not-custom-spawner");
            return;
        }
        linkService.start(player, data);
    }

    private void unlink(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "only-player");
            return;
        }
        SpawnerData data = targetedSpawner(player);
        if (data == null) {
            messages.send(player, "not-custom-spawner");
            return;
        }
        spawnerManager.setTarget(data, null);
        messages.send(player, "unlink-success");
    }

    private void remove(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "only-player");
            return;
        }
        if (!player.hasPermission("advancedspawners.remove")) {
            messages.send(player, "no-permission");
            return;
        }
        SpawnerData data = targetedSpawner(player);
        if (data == null) {
            messages.send(player, "not-custom-spawner");
            return;
        }
        spawnerManager.dismantleToPlayer(data, player, config.removeReturnsStoredItems());
        messages.send(player, "spawner-removed-gui");
    }

    private SpawnerData targetedSpawner(Player player) {
        Block block = player.getTargetBlockExact(6);
        if (block == null || block.getType() != Material.SPAWNER) return null;
        return spawnerManager.byBlock(block).orElse(null);
    }

    private EntityType parseType(String text) {
        try {
            return EntityType.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private int parsePositive(String text, int def) {
        try {
            return Math.max(1, Integer.parseInt(text));
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return filter(List.of("give", "giveempty", "reload", "info", "link", "unlink", "remove"), args[0]);
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("giveempty"))) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return filter(List.of("ZOMBIE", "SKELETON", "CREEPER", "SPIDER", "BLAZE", "IRON_GOLEM"), args[2]);
        }
        if (args.length == 5 && args[0].equalsIgnoreCase("give")) return filter(List.of("egg"), args[4]);
        return List.of();
    }

    private List<String> filter(List<String> values, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lower)) result.add(value);
        }
        return result;
    }
}
