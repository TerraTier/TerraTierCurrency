package com.terratier.currency.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.terratier.currency.currency.Currency;
import com.terratier.currency.currency.CurrencyIds;
import com.terratier.currency.currency.CurrencyList;

/**
 * Manages player currency balances
 */
public class CurrencyManager {
    private static CurrencyManager instance;

    // Structure: playerUUID -> currencyId -> amount
    private final Map<UUID, Map<String, Double>> playerCurrencies = new HashMap<>();

    private CurrencyManager() {
    }

    public static CurrencyManager getInstance() {
        if (instance == null) {
            instance = new CurrencyManager();
        }
        return instance;
    }

    public synchronized void loadBalances(File file) {
        if (!file.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection balancesSection = config.getConfigurationSection("balances");
        if (balancesSection == null) {
            return;
        }

        for (String playerId : balancesSection.getKeys(false)) {
            ConfigurationSection playerSection = balancesSection.getConfigurationSection(playerId);
            UUID uuid;
            try {
                uuid = UUID.fromString(playerId);
            } catch (IllegalArgumentException e) {
                continue;
            }

            Map<String, Double> currencyMap = new HashMap<>();
            for (String currencyId : playerSection.getKeys(false)) {
                double amount = playerSection.getDouble(currencyId, 0.0);
                currencyMap.merge(CurrencyIds.normalize(currencyId), amount, Double::sum);
            }
            playerCurrencies.put(uuid, currencyMap);
        }
    }

    public synchronized void saveBalances(File file) {
        FileConfiguration config = new YamlConfiguration();
        ConfigurationSection balancesSection = config.createSection("balances");
        for (Map.Entry<UUID, Map<String, Double>> playerEntry : playerCurrencies.entrySet()) {
            ConfigurationSection playerSection = balancesSection.createSection(playerEntry.getKey().toString());
            for (Map.Entry<String, Double> currencyEntry : playerEntry.getValue().entrySet()) {
                String currencyId = CurrencyIds.normalize(currencyEntry.getKey());
                double amount = playerSection.getDouble(currencyId, 0.0) + currencyEntry.getValue();
                playerSection.set(currencyId, amount);
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void ensurePlayerData(UUID playerUUID) {
        Map<String, Double> currencies = playerCurrencies.computeIfAbsent(playerUUID, k -> new HashMap<>());
        mergeLegacyCoinBalance(currencies);
        for (Currency currency : CurrencyList.getInstance().getAllCurrencies().values()) {
            currencies.putIfAbsent(CurrencyIds.normalize(currency.getId()), 0.0);
        }
    }

    /**
     * Get a player's currency balance
     * @param playerUUID The player's UUID
     * @param currencyId The currency ID
     * @return The balance, or 0 if the player/currency doesn't exist
     */
    public synchronized double getBalance(UUID playerUUID, String currencyId) {
        return playerCurrencies
            .getOrDefault(playerUUID, new HashMap<>())
            .getOrDefault(CurrencyIds.normalize(currencyId), 0.0);
    }

    /**
     * Set a player's currency balance
     * @param playerUUID The player's UUID
     * @param currencyId The currency ID
     * @param amount The amount to set
     */
    public synchronized void setBalance(UUID playerUUID, String currencyId, double amount) {
        amount = Math.max(0, amount); // Prevent negative balances
        playerCurrencies
            .computeIfAbsent(playerUUID, k -> new HashMap<>())
            .put(CurrencyIds.normalize(currencyId), amount);
    }

    /**
     * Add currency to a player
     * @param playerUUID The player's UUID
     * @param currencyId The currency ID
     * @param amount The amount to add
     */
    public synchronized void addBalance(UUID playerUUID, String currencyId, double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive. Use subtractBalance for subtraction.");
        }
        double current = getBalance(playerUUID, currencyId);
        String normalizedCurrencyId = CurrencyIds.normalize(currencyId);
        setBalance(playerUUID, normalizedCurrencyId, current + amount);
        if (normalizedCurrencyId.equals(CurrencyIds.COINS)) {
            double currentTotal = getBalance(playerUUID, CurrencyIds.TOTAL_COINS);
            setBalance(playerUUID, CurrencyIds.TOTAL_COINS, currentTotal + amount);
        }
    }

    /**
     * Subtract currency from a player
     * @param playerUUID The player's UUID
     * @param currencyId The currency ID
     * @param amount The amount to subtract
     * @return true if successful, false if player has insufficient balance
     */
    public synchronized boolean subtractBalance(UUID playerUUID, String currencyId, double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive. Use addBalance for addition.");
        }
        double current = getBalance(playerUUID, currencyId);
        if (current < amount) {
            return false; // Insufficient balance
        }
        setBalance(playerUUID, currencyId, current - amount);
        return true;
    }

    /**
     * Check if a player has at least a certain amount of currency
     * @param playerUUID The player's UUID
     * @param currencyId The currency ID
     * @param amount The amount to check
     * @return true if the player has enough balance
     */
    public synchronized boolean hasBalance(UUID playerUUID, String currencyId, double amount) {
        return getBalance(playerUUID, currencyId) >= amount;
    }

    private void mergeLegacyCoinBalance(Map<String, Double> currencies) {
        Double legacyCoins = currencies.remove("coin");
        if (legacyCoins != null) {
            currencies.merge(CurrencyIds.COINS, legacyCoins, Double::sum);
        }
    }
}
