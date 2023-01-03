package com.sewerynkamil.fxscraping;

public enum Currency {
    GBP("GREAT BRITISH POUND", "GBP"),
    EUR("EURO", "EUR"),
    JPY("JAPANESE YEN", "JPY"),
    CAD("CANADIAN DOLLAR", "CAD");

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
        return String.format("%s - %s", currencyName, currencySymbol);
    }
}
