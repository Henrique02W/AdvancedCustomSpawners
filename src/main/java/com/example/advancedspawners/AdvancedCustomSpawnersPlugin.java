package com.example.advancedspawners;

import com.example.advancedspawners.command.SpawnerCommand;
import com.example.advancedspawners.config.PluginConfig;
import com.example.advancedspawners.gui.GuiListener;
import com.example.advancedspawners.gui.GuiManager;
import com.example.advancedspawners.hologram.HologramManager;
import com.example.advancedspawners.integration.PlaceholderIntegration;
import com.example.advancedspawners.listener.SpawnerListener;
import com.example.advancedspawners.service.EconomyService;
import com.example.advancedspawners.service.ItemFactory;
import com.example.advancedspawners.service.LinkService;
import com.example.advancedspawners.service.MessageService;
import com.example.advancedspawners.service.SpawnerManager;
import com.example.advancedspawners.storage.SpawnerStorage;
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
        spawnerManager.applyRuntimeSettings();
        hologramManager.refreshAll(spawnerManager.all());
    }
}
