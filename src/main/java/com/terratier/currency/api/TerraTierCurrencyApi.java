package com.terratier.currency.api;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.terratier.currency.currency.Currency;

public interface TerraTierCurrencyApi {
    String COINS = "coins";
    String TOTAL_COINS = "total-coins";

    double getBalance(UUID playerId, String currencyId);

    void setBalance(UUID playerId, String currencyId, double amount);

    void addBalance(UUID playerId, String currencyId, double amount);

    boolean subtractBalance(UUID playerId, String currencyId, double amount);

    boolean hasBalance(UUID playerId, String currencyId, double amount);

    boolean hasCurrency(String currencyId);

    Currency getCurrency(String currencyId);

    Set<String> getCurrencyIds();

    Map<String, Currency> getCurrencies();
}
