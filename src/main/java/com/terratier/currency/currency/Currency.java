package com.terratier.currency.currency;

/**
 * Represents a currency type with display properties
 */
public class Currency {
    private final String id;
    private final String displayName;
    private final String icon;
    
    public Currency(String id, String displayName, String icon) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
    }
    
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    @Override
    public String toString() {
        return displayName + " " + icon;
    }
}
