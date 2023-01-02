package com.sewerynkamil.fxscraping;

public enum Currency {
    GBP("British Pound", "GBP"),
    EUR("Euro", "EUR"),
    JPY("Japanese Yen", "JPY"),
    CAD("Canadian Dollar", "CAD");

    private final String currencyName;
    private final String currencySymbol;

    Currency(String currencyName, String currencySymbol) {
        this.currencyName = currencyName;
        this.currencySymbol = currencySymbol;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public String getLabel() {
        return String.format("%s (%s)", currencyName, currencySymbol);
    }
}
