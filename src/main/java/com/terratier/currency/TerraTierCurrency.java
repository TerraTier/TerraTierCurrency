package com.terratier.currency;

import java.io.File;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.terratier.currency.api.TerraTierCurrencyApi;
import com.terratier.currency.api.TerraTierCurrencyApiProvider;
import com.terratier.currency.commands.CurrencyCommand;
import com.terratier.currency.config.CurrencyConfig;
import com.terratier.currency.currency.CurrencyList;
import com.terratier.currency.listeners.CoinRegionListener;
import com.terratier.currency.listeners.PlayerListener;
import com.terratier.currency.managers.CurrencyManager;
import com.terratier.currency.managers.PluginManager;
import com.terratier.currency.placeholders.TerraTierCurrencyExpansion;

public class TerraTierCurrency extends JavaPlugin {
    public static IntegerFlag REQUIRED_TOTAL_COINS_FLAG;

    private final CurrencyManager currencyManager = CurrencyManager.getInstance();
    private final CurrencyList currencyList = CurrencyList.getInstance();
    private File balanceFile;

    @Override
    public void onLoad() {
        try {
            IntegerFlag flag = new IntegerFlag("required-total-coins");
            WorldGuard.getInstance().getFlagRegistry().register(flag);
            REQUIRED_TOTAL_COINS_FLAG = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = WorldGuard.getInstance().getFlagRegistry().get("required-total-coins");
            if (existing instanceof IntegerFlag) {
                REQUIRED_TOTAL_COINS_FLAG = (IntegerFlag) existing;
            } else {
                getLogger().severe("WorldGuard flag 'required-total-coins' already exists with a different type.");
            }
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new CurrencyConfig(this).load();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        balanceFile = new File(getDataFolder(), "balances.yml");
        currencyManager.loadBalances(balanceFile);

        PluginManager.getInstance().initialize();
        getServer().getServicesManager().register(
                TerraTierCurrencyApi.class,
                new TerraTierCurrencyApiProvider(currencyManager, currencyList),
                this,
                ServicePriority.Normal
        );
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new CoinRegionListener(), this);
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new TerraTierCurrencyExpansion(this, currencyManager).register();
            getLogger().info("Registered PlaceholderAPI placeholders.");
        }

        CurrencyCommand cmd = new CurrencyCommand();
        if (getCommand("terratiercurrency") != null) {
            getCommand("terratiercurrency").setExecutor(cmd);
            getCommand("terratiercurrency").setTabCompleter(cmd);
        }
        if (getCommand("ttc") != null) {
            getCommand("ttc").setExecutor(cmd);
            getCommand("ttc").setTabCompleter(cmd);
        }

        getLogger().info("TerraTierCurrency enabled with " + currencyList.getAllCurrencies().size() + " currency types.");
    }

    @Override
    public void onDisable() {
        if (balanceFile == null) {
            balanceFile = new File(getDataFolder(), "balances.yml");
        }
        getServer().getServicesManager().unregisterAll(this);
        currencyManager.saveBalances(balanceFile);
        getLogger().info("TerraTierCurrency has been disabled!");
    }
}
