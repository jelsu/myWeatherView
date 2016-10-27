package com.teaching.jelus.myweatherview;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MyApp extends Application {
    private static Context sContext;
    private String url = "http://api.openweathermap.org/data/2.5/weather/";
    private String cityId = "498817";
    private String appId = "98fb5e0dcef9e5de3219365edf223805";

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.sContext = getApplicationContext();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        GetDataTask getDataTask = new GetDataTask();
        Future<String> jsonData = pool.submit(getDataTask);
        try {
            JsonParserTask jsonParserTask = new JsonParserTask(jsonData.get());
            Future<WeatherData> jsonParcer = pool.submit(jsonParserTask);
            WeatherData weatherData = jsonParcer.get();
            DBWriterTask dbWriterTask = new DBWriterTask(weatherData);
            pool.submit(dbWriterTask);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        pool.shutdown();
    }

    public static Context getAppContext() {
        return MyApp.sContext;
    }
}

