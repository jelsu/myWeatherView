package com.teaching.jelus.myweatherview;

import android.graphics.Bitmap;

public class ForecastData {
    private int temperature;
    private String cityName;
    private String weatherDescription;
    private String data;
    private Bitmap image;

    public ForecastData(int temperature,
                        String cityName,
                        String weatherDescription,
                        String data,
                        Bitmap image) {
        this.temperature = temperature;
        this.cityName = cityName;
        this.weatherDescription = weatherDescription;
        this.data = data;
        this.image = image;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
