package com.teaching.jelus.myweatherview;

public class WeatherData {
    private String city;
    private int temperature;
    private String weather;

    public WeatherData(String city, int temperature, String weather) {
        this.city = city;
        this.temperature = temperature;
        this.weather = weather;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }
}
