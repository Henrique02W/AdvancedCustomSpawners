package io.github.henrique02w.advancedcustomspawners.service;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class EconomyService {
    private final JavaPlugin plugin;

    public EconomyService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Procura o provedor de economia a cada uso, em vez de guardá-lo no enable.
     * Os plugins de economia (EssentialsX, CMI etc.) só registram o serviço no Vault durante o
     * próprio enable, que pode acontecer depois do enable deste plugin.
     */
    private Economy current() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) return null;
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        return provider == null ? null : provider.getProvider();
    }

    public void reload() {
        // Nada para recarregar: o provedor é resolvido sob demanda em current().
    }

    /** Registra no console se a economia está conectada, para facilitar o diagnóstico. */
    public void logStatus() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            plugin.getLogger().warning("Vault nao encontrado ou desativado. Upgrades em dinheiro ficam indisponiveis.");
            return;
        }
        Economy economy = current();
        if (economy == null) {
            plugin.getLogger().warning("Vault encontrado, mas nenhum plugin de economia registrou um provedor (ex.: EssentialsX).");
        } else {
            plugin.getLogger().info("Economia conectada via Vault: " + economy.getName());
        }
    }

    public boolean available() {
        return current() != null;
    }

    public String format(double amount) {
        Economy economy = current();
        if (economy == null) return String.format("$%,.2f", amount);
        return economy.format(amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (amount <= 0D) return true;
        Economy economy = current();
        if (economy == null) return false;
        if (!economy.has(player, amount)) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
}
