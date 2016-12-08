package com.teaching.jelus.myweatherview;

import android.graphics.Bitmap;

public class ForecastData {
    private int tempMin;
    private int tempMax;
    private String description;
    private String data;
    private Bitmap image;

    public ForecastData(int tempMin, int tempMax, String description, String data, Bitmap image) {
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.description = description;
        this.data = data;
        this.image = image;
    }

    public int getTempMin() {
        return tempMin;
    }

    public void setTempMin(int tempMin) {
        this.tempMin = tempMin;
    }

    public int getTempMax() {
        return tempMax;
    }

    public void setTempMax(int tempMax) {
        this.tempMax = tempMax;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
