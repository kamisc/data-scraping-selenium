package com.sewerynkamil.weather;

public enum WeatherLocation {
    IT("IT-FAWC", "TURIN"),
    JP("JP-0H2L", "TOKYO"),
    CA("CA-ZV5S", "MONTREAL"),
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
