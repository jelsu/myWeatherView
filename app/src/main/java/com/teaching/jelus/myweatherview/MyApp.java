package com.teaching.jelus.myweatherview;

import android.app.Application;

public class MyApp extends Application {
    private String url = "http://api.openweathermap.org/data/2.5/weather/";
    private String cityId = "498817";
    private String appId = "98fb5e0dcef9e5de3219365edf223805";

    @Override
    public void onCreate() {
        super.onCreate();
        new WeatherDataTask().execute(url + "?id=" + cityId + "&appid=" + appId + "&units=metric");
    }
}
