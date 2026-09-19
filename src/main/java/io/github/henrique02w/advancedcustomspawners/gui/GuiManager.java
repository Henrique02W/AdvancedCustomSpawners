package io.github.henrique02w.advancedcustomspawners.gui;

import io.github.henrique02w.advancedcustomspawners.config.PluginConfig;
import io.github.henrique02w.advancedcustomspawners.model.SpawnerData;
import io.github.henrique02w.advancedcustomspawners.model.UpgradeType;
import io.github.henrique02w.advancedcustomspawners.service.EconomyService;
import io.github.henrique02w.advancedcustomspawners.service.ItemFactory;
import io.github.henrique02w.advancedcustomspawners.service.MessageService;
import io.github.henrique02w.advancedcustomspawners.service.SpawnerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;

public final class GuiManager {
    public static final int COLLECT_SLOT = 49;
    public static final int LINK_SLOT = 47;
    public static final int UNLINK_SLOT = 51;
    public static final int FUEL_SLOT = 22;
    public static final int TOGGLE_SLOT = 40;
    public static final int REMOVE_SLOT = 53;

    private final PluginConfig config;
    private final MessageService messages;
    private final SpawnerManager spawnerManager;
    private final ItemFactory itemFactory;
    private final EconomyService economyService;

    public GuiManager(Plugin plugin, PluginConfig config, MessageService messages, SpawnerManager spawnerManager, ItemFactory itemFactory, EconomyService economyService) {
        this.config = config;
        this.messages = messages;
        this.spawnerManager = spawnerManager;
        this.itemFactory = itemFactory;
        this.economyService = economyService;
    }

    public void open(Player player, SpawnerData data) {
        SpawnerGuiHolder holder = new SpawnerGuiHolder(data.id());
        Inventory inventory = Bukkit.createInventory(holder, 54, messages.component("&8Spawner " + data.entityType().name()));
        holder.inventory(inventory);
        render(inventory, data);
        player.openInventory(inventory);
    }

    public void render(Inventory inventory, SpawnerData data) {
        inventory.clear();
        paintFrame(inventory);
        inventory.setItem(4, itemFactory.button(Material.SPAWNER, "&bSpawner &f" + data.entityType().name(),
                List.of("&7Estado: &f" + (data.enabled() ? "Ligado" : "Desligado"), "&7Combustivel: &f" + fuelText(data), "&7Destino: &f" + (data.targetLocation() == null ? "desconectado" : "conectado"))));

        int[] upgradeSlots = {10, 11, 12, 13, 14, 15, 16};
        UpgradeType[] types = UpgradeType.values();
        for (int i = 0; i < types.length; i++) {
            UpgradeType type = types[i];
            int level = data.upgrade(type);
            int max = config.maxLevel(type);
            int next = Math.min(max, level + 1);
            String key = type.name().toLowerCase();
            PluginConfig.GuiItemDefinition def = config.getGuiItem(key);
            inventory.setItem(upgradeSlots[i], itemFactory.button(def.material(),
                    "&b" + type.display() + " &7(" + level + "/" + max + ")",
                    List.of("&8Upgrade individual", "&7Clique para evoluir.", "&7Custo: &a" + costText(type, level, max, next)), def.customModelData()));
        }

        inventory.setItem(FUEL_SLOT, itemFactory.button(config.getGuiItem("fuel-button").material(), "&6Combustivel",
                fuelLore(data), config.getGuiItem("fuel-button").customModelData()));
        inventory.setItem(TOGGLE_SLOT, itemFactory.button(data.enabled() ? config.getGuiItem("toggle-on").material() : config.getGuiItem("toggle-off").material(),
                data.enabled() ? "&aSpawner ligado" : "&eSpawner desligado",
                List.of("&7Clique para alternar.", data.enabled() ? "&7Spawnando normalmente." : "&7Nao ira spawnar mobs."),
                data.enabled() ? config.getGuiItem("toggle-on").customModelData() : config.getGuiItem("toggle-off").customModelData()));
        inventory.setItem(COLLECT_SLOT, itemFactory.button(config.getGuiItem("collect").material(), "&aColetar armazenamento",
                List.of("&7Itens armazenados: &f" + data.storedItemCount(), "&7Fila de envio: &f" + data.pendingRoute().size() + " stacks"), config.getGuiItem("collect").customModelData()));
        inventory.setItem(LINK_SLOT, itemFactory.button(config.getGuiItem("link").material(), "&bConectar destino",
                List.of("&7Status: &f" + (data.targetLocation() == null ? "desconectado" : data.targetLocation().compact()), "&7Clique e depois selecione um bau/barrel."), config.getGuiItem("link").customModelData()));
        inventory.setItem(UNLINK_SLOT, itemFactory.button(config.getGuiItem("unlink").material(), "&cRemover destino",
                List.of("&7Volta para armazenamento interno."), config.getGuiItem("unlink").customModelData()));
        inventory.setItem(REMOVE_SLOT, itemFactory.button(config.getGuiItem("remove").material(), "&cRemover spawner",
                List.of("&7Devolve o spawner ao jogador.", "&7Tambem devolve itens armazenados.", "&cClique duas vezes para confirmar."), config.getGuiItem("remove").customModelData()));

        int slot = 28;
        for (ItemStack item : data.storage()) {
            if (slot == 35) slot = 37;
            if (slot == 40) slot = 41;
            if (slot >= 44) break;
            inventory.setItem(slot++, item.clone());
        }
    }

    public Optional<SpawnerData> dataFrom(Inventory inventory) {
        if (!(inventory.getHolder() instanceof SpawnerGuiHolder holder)) return Optional.empty();
        return spawnerManager.byId(holder.spawnerId());
    }

    public boolean takeUpgradeCost(Player player, double amount) {
        if (amount <= 0D) return true;
        if (config.economyEnabled()) {
            if (economyService.available()) return economyService.withdraw(player, amount);
            if (config.requireVault()) return false;
        }
        int itemAmount = (int) Math.ceil(amount);
        if (!player.getInventory().containsAtLeast(new ItemStack(Material.EMERALD), itemAmount)) return false;
        player.getInventory().removeItem(new ItemStack(Material.EMERALD, itemAmount));
        return true;
    }



    private String fuelText(SpawnerData data) {
        int level = data.upgrade(UpgradeType.FUEL);
        if (level >= config.maxLevel(UpgradeType.FUEL) && config.fuelConsumptionMultiplier(level) <= 0D) {
            return "infinito";
        }
        return data.fuelTicks() + " ticks";
    }

    private void paintFrame(Inventory inventory) {
        ItemStack border = itemFactory.button(Material.GRAY_STAINED_GLASS_PANE, "&8", List.of());
        ItemStack accent = itemFactory.button(Material.CYAN_STAINED_GLASS_PANE, "&8", List.of());
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, border);
        }
        for (int slot : new int[]{0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 48, 50, 52}) {
            inventory.setItem(slot, accent);
        }
    }

    private String costText(UpgradeType type, int level, int max, int next) {
        if (level >= max) return "MAX";
        double cost = config.upgradeCost(type, next);
        if (config.economyEnabled() && economyService.available()) return economyService.format(cost);
        if (config.economyEnabled() && config.requireVault()) return "Vault indisponivel";
        return ((int) Math.ceil(cost)) + " esmeraldas";
    }

    private List<String> fuelLore(SpawnerData data) {
        List<String> lore = new java.util.ArrayList<>();
        lore.add("&7Atual: &f" + fuelText(data));
        lore.add("&7Clique em um combustivel no inventario.");
        lore.add("&7Ultimo nivel do upgrade = infinito.");
        lore.add("");
        lore.add("&6Combustiveis aceitos:");
        config.fuels().entrySet().stream()
                .sorted(java.util.Comparator.comparing(entry -> entry.getKey().name()))
                .forEach(entry -> lore.add("&8- &f" + entry.getKey().name() + " &7(" + entry.getValue().effectiveTicks() + " ticks)"));
        return lore;
    }
}
