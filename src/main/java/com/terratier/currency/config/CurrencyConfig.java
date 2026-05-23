package com.terratier.currency.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.terratier.currency.currency.Currency;
import com.terratier.currency.currency.CurrencyIds;
import com.terratier.currency.currency.CurrencyList;

public class CurrencyConfig {
    private final JavaPlugin plugin;
    private final CurrencyList currencyList = CurrencyList.getInstance();

    public CurrencyConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection currencySection = config.getConfigurationSection("currencies");
        if (currencySection == null) {
            return;
        }

        for (String currencyId : currencySection.getKeys(false)) {
            if (CurrencyIds.isLegacyCoinId(currencyId)) {
                plugin.getLogger().warning("Ignoring legacy currency id 'coin'. Use 'coins' instead.");
                continue;
            }

            ConfigurationSection section = currencySection.getConfigurationSection(currencyId);
            if (section == null) {
                continue;
            }

            String displayName = section.getString("display-name", currencyId);
            String icon = section.getString("icon", "\uD83D\uDCB0");
            currencyList.registerCurrency(new Currency(currencyId, displayName, icon));
        }
    }
}
