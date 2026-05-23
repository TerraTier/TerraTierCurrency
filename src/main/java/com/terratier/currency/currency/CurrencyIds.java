package com.terratier.currency.currency;

import java.util.Locale;

public final class CurrencyIds {
    public static final String COINS = "coins";
    public static final String TOTAL_COINS = "total-coins";

    private CurrencyIds() {
    }

    public static String normalize(String id) {
        String normalized = id.toLowerCase(Locale.ROOT);
        if (normalized.equals("coin")) {
            return COINS;
        }
        return normalized;
    }

    public static boolean isLegacyCoinId(String id) {
        return id.equalsIgnoreCase("coin");
    }
}
