package com.teaching.jelus.myweatherview;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {
    private static Context sContext;
    private String url = "http://api.openweathermap.org/data/2.5/weather/";
    private String cityId = "498817";
    private String appId = "98fb5e0dcef9e5de3219365edf223805";
    private DatabaseHelper mDatabaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.sContext = getApplicationContext();
        new WeatherDataTask().execute(url + "?id=" + cityId + "&appid=" + appId + "&units=metric");
    }

    public static Context getAppContext() {
        return MyApp.sContext;
    }
}
