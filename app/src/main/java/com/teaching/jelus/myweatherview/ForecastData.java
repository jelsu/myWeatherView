package com.teaching.jelus.myweatherview;

import android.graphics.Bitmap;

public class ForecastData {
    private int temperatureMin;
    private int temperatureMax;
    private String weatherDescription;
    private String data;
    private Bitmap image;

    public ForecastData(int temperatureMin, int temperatureMax, String weatherDescription, String data, Bitmap image) {
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.weatherDescription = weatherDescription;
        this.data = data;
        this.image = image;
    }

    public int getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMin(int temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public int getTemperatureMax() {
        return temperatureMax;
    }

    public void setTemperatureMax(int temperatureMax) {
        this.temperatureMax = temperatureMax;
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
