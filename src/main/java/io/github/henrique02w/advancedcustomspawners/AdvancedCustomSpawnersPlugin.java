package io.github.henrique02w.advancedcustomspawners;

import io.github.henrique02w.advancedcustomspawners.command.SpawnerCommand;
import io.github.henrique02w.advancedcustomspawners.config.PluginConfig;
import io.github.henrique02w.advancedcustomspawners.gui.GuiListener;
import io.github.henrique02w.advancedcustomspawners.gui.GuiManager;
import io.github.henrique02w.advancedcustomspawners.hologram.HologramManager;
import io.github.henrique02w.advancedcustomspawners.integration.PlaceholderIntegration;
import io.github.henrique02w.advancedcustomspawners.listener.SpawnerListener;
import io.github.henrique02w.advancedcustomspawners.service.EconomyService;
import io.github.henrique02w.advancedcustomspawners.service.ItemFactory;
import io.github.henrique02w.advancedcustomspawners.service.LinkService;
import io.github.henrique02w.advancedcustomspawners.service.MessageService;
import io.github.henrique02w.advancedcustomspawners.service.SpawnerManager;
import io.github.henrique02w.advancedcustomspawners.storage.SpawnerStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class AdvancedCustomSpawnersPlugin extends JavaPlugin {
    private PluginConfig pluginConfig;
    private MessageService messages;
    private SpawnerStorage storage;
    private ItemFactory itemFactory;
    private EconomyService economyService;
    private SpawnerManager spawnerManager;
    private HologramManager hologramManager;
    private LinkService linkService;
    private GuiManager guiManager;
    private BukkitTask saveTask;
    private BukkitTask retryTask;
    private BukkitTask forcedSpawnTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        saveResource("messages.yml", false);

        this.pluginConfig = new PluginConfig(this);
        this.messages = new MessageService(this);
        this.storage = new SpawnerStorage(this);
        this.itemFactory = new ItemFactory(this, pluginConfig);
        this.economyService = new EconomyService(this);
        this.hologramManager = new HologramManager(this, pluginConfig);
        this.spawnerManager = new SpawnerManager(this, pluginConfig, storage, itemFactory, hologramManager);
        this.linkService = new LinkService(pluginConfig, messages, spawnerManager);
        this.guiManager = new GuiManager(this, pluginConfig, messages, spawnerManager, itemFactory, economyService);

        spawnerManager.load();
        hologramManager.spawnAll(spawnerManager.all());

        SpawnerCommand command = new SpawnerCommand(this, pluginConfig, messages, itemFactory, spawnerManager, linkService);
        getCommand("spawner").setExecutor(command);
        getCommand("spawner").setTabCompleter(command);

        Bukkit.getPluginManager().registerEvents(new SpawnerListener(this, pluginConfig, messages, itemFactory, spawnerManager, linkService, guiManager), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(guiManager, pluginConfig, messages, spawnerManager, linkService, economyService), this);

        long saveInterval = Math.max(30L, pluginConfig.saveIntervalSeconds()) * 20L;
        saveTask = Bukkit.getScheduler().runTaskTimer(this, () -> spawnerManager.save(false), saveInterval, saveInterval);
        retryTask = Bukkit.getScheduler().runTaskTimer(this, () -> spawnerManager.retryPendingRoutes(), 100L, 100L);
        forcedSpawnTask = Bukkit.getScheduler().runTaskTimer(this, () -> spawnerManager.tickForcedEnvironmentSpawners(), 20L, 20L);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PlaceholderIntegration.register(this, spawnerManager);
        }

        // Roda no primeiro tick, depois que todos os plugins (inclusive o de economia) terminaram de ativar.
        Bukkit.getScheduler().runTask(this, economyService::logStatus);

        getLogger().info("AdvancedCustomSpawners enabled with " + spawnerManager.all().size() + " spawners loaded.");
    }

    @Override
    public void onDisable() {
        if (saveTask != null) saveTask.cancel();
        if (retryTask != null) retryTask.cancel();
        if (forcedSpawnTask != null) forcedSpawnTask.cancel();
        if (spawnerManager != null) spawnerManager.save(true);
        if (hologramManager != null) hologramManager.removeAll();
    }

    public void reloadEverything() {
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        pluginConfig.reload();
        messages.reload();
        economyService.reload();
        economyService.logStatus();
        spawnerManager.applyRuntimeSettings();
        hologramManager.refreshAll(spawnerManager.all());
    }
}
