package com.terratier.currency.api;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.terratier.currency.currency.Currency;
import com.terratier.currency.currency.CurrencyList;
import com.terratier.currency.managers.CurrencyManager;

public class TerraTierCurrencyApiProvider implements TerraTierCurrencyApi {
    private final CurrencyManager currencyManager;
    private final CurrencyList currencyList;

    public TerraTierCurrencyApiProvider(CurrencyManager currencyManager, CurrencyList currencyList) {
        this.currencyManager = currencyManager;
        this.currencyList = currencyList;
    }

    @Override
    public double getBalance(UUID playerId, String currencyId) {
        return currencyManager.getBalance(playerId, currencyId);
    }

    @Override
    public void setBalance(UUID playerId, String currencyId, double amount) {
        currencyManager.setBalance(playerId, currencyId, amount);
    }

    @Override
    public void addBalance(UUID playerId, String currencyId, double amount) {
        currencyManager.addBalance(playerId, currencyId, amount);
    }

    @Override
    public boolean subtractBalance(UUID playerId, String currencyId, double amount) {
        return currencyManager.subtractBalance(playerId, currencyId, amount);
    }

    @Override
    public boolean hasBalance(UUID playerId, String currencyId, double amount) {
        return currencyManager.hasBalance(playerId, currencyId, amount);
    }

    @Override
    public boolean hasCurrency(String currencyId) {
        return currencyList.hasCurrency(currencyId);
    }

    @Override
    public Currency getCurrency(String currencyId) {
        return currencyList.getCurrency(currencyId);
    }

    @Override
    public Set<String> getCurrencyIds() {
        return currencyList.getAllCurrencies().keySet();
    }

    @Override
    public Map<String, Currency> getCurrencies() {
        return currencyList.getAllCurrencies();
    }
}
