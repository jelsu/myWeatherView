package com.teaching.jelus.myweatherview;

public class WeatherData {
    private String cityName;
    private int temperature;
    private String weatherDescription;
    private String iconCode;

    public WeatherData(String weatherDescription, int temperature, String city, String iconCode) {
        this.weatherDescription = weatherDescription;
        this.temperature = temperature;
        this.cityName = city;
        this.iconCode = iconCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public String getIconCode() {
        return iconCode;
    }

    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }
}
