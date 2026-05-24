package com.terratier.currency.placeholders;

import java.text.DecimalFormat;
import java.util.Locale;

import org.bukkit.OfflinePlayer;

import com.terratier.currency.TerraTierCurrency;
import com.terratier.currency.currency.CurrencyIds;
import com.terratier.currency.managers.CurrencyManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class TerraTierCurrencyExpansion extends PlaceholderExpansion {
    private static final DecimalFormat WHOLE_NUMBER_FORMAT = new DecimalFormat("#,##0");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.##");

    private final TerraTierCurrency plugin;
    private final CurrencyManager currencyManager;

    public TerraTierCurrencyExpansion(TerraTierCurrency plugin, CurrencyManager currencyManager) {
        this.plugin = plugin;
        this.currencyManager = currencyManager;
    }

    @Override
    public String getIdentifier() {
        return "terratiercurrency";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || player.getUniqueId() == null) {
            return "";
        }

        String normalizedParams = params.toLowerCase(Locale.ROOT);
        switch (normalizedParams) {
            case "coins":
                return formatWhole(currencyManager.getBalance(player.getUniqueId(), CurrencyIds.COINS));
            case "coins_decimal":
                return formatDecimal(currencyManager.getBalance(player.getUniqueId(), CurrencyIds.COINS));
            case "total_coins":
            case "total-coins":
                return formatWhole(currencyManager.getBalance(player.getUniqueId(), CurrencyIds.TOTAL_COINS));
            case "total_coins_decimal":
            case "total-coins_decimal":
                return formatDecimal(currencyManager.getBalance(player.getUniqueId(), CurrencyIds.TOTAL_COINS));
            default:
                return null;
        }
    }

    private String formatWhole(double value) {
        return WHOLE_NUMBER_FORMAT.format(value);
    }

    private String formatDecimal(double value) {
        return DECIMAL_FORMAT.format(value);
    }
}
