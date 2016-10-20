package com.teaching.jelus.myweatherview;

public class WeatherData {
    private String city;
    private int temperature;
    private String weather;
    private String iconCode;

    public WeatherData(String weather, int temperature, String city, String iconCode) {
        this.weather = weather;
        this.temperature = temperature;
        this.city = city;
        this.iconCode = iconCode;
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

    public String getIconCode() {
        return iconCode;
    }

    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }
}
