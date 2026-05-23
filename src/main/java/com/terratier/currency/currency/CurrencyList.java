package com.terratier.currency.currency;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manages all available currencies in the plugin
 */
public class CurrencyList {
    private static CurrencyList instance;
    private final Map<String, Currency> currencies = new HashMap<>();

    private CurrencyList() {
        registerCurrency(new Currency(CurrencyIds.COINS, "Coins", "\uD83D\uDCB0"));
        registerCurrency(new Currency(CurrencyIds.TOTAL_COINS, "Total Coins", "\uD83D\uDCB0"));
    }

    public static CurrencyList getInstance() {
        if (instance == null) {
            instance = new CurrencyList();
        }
        return instance;
    }

    /**
     * Register a new currency
     * @param currency The currency to register
     */
    public void registerCurrency(Currency currency) {
        if (CurrencyIds.isLegacyCoinId(currency.getId())) {
            return;
        }

        String id = CurrencyIds.normalize(currency.getId());
        String displayName = currency.getDisplayName();
        if (id.equals(CurrencyIds.COINS) && displayName.equalsIgnoreCase("Coin")) {
            displayName = "Coins";
        }

        currencies.put(id, new Currency(id, displayName, currency.getIcon()));
    }

    /**
     * Load configured currencies from config
     */
    public void loadFromConfig(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("currencies");
        if (section == null) {
            return;
        }

        for (String currencyId : section.getKeys(false)) {
            ConfigurationSection currencySection = section.getConfigurationSection(currencyId);
            if (currencySection == null) {
                continue;
            }

            String displayName = currencySection.getString("display-name", currencyId);
            String icon = currencySection.getString("icon", "\uD83D\uDCB0");
            registerCurrency(new Currency(currencyId.toLowerCase(Locale.ROOT), displayName, icon));
        }
    }

    /**
     * Get a currency by ID
     * @param id The currency ID
     * @return The currency, or null if not found
     */
    public Currency getCurrency(String id) {
        return currencies.get(CurrencyIds.normalize(id));
    }

    /**
     * Check if a currency exists
     * @param id The currency ID
     * @return true if the currency exists
     */
    public boolean hasCurrency(String id) {
        return currencies.containsKey(CurrencyIds.normalize(id));
    }

    /**
     * Get all currencies
     * @return Map of all currencies
     */
    public Map<String, Currency> getAllCurrencies() {
        return new HashMap<>(currencies);
    }
}
