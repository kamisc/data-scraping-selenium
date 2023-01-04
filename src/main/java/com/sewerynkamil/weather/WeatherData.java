package com.sewerynkamil.weather;

import java.io.Serializable;

public class WeatherData implements Serializable {
    private double temperature;
    private double participation;
    private double pressure;

    public WeatherData(double temperature, double participation, double pressure) {
        this.temperature = temperature;
        this.participation = participation;
        this.pressure = pressure;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getParticipation() {
        return participation;
    }

    public double getPressure() {
        return pressure;
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "temperature=" + temperature +
                ", participation=" + participation +
                ", pressure=" + pressure +
                '}';
    }
}
