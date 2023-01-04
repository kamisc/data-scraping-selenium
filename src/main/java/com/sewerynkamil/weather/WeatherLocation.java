package com.sewerynkamil.weather;

public enum WeatherLocation {
    IT("16061", "TURIN"),
    JP("47683", "TOKYO"),
    CA("71612", "MONTREAL"),
    GB("EGLC0", "LONDON");

    private final String websiteCode;
    private final String city;
    WeatherLocation(String websiteCode, String city) {
        this.websiteCode = websiteCode;
        this.city = city;
    }

    public String getWebsiteCode() {
        return websiteCode;
    }

    public String getCity() {
        return city;
    }
}
