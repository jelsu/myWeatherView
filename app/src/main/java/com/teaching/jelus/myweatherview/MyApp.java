package com.teaching.jelus.myweatherview;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApp extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.sContext = getApplicationContext();
        if (isConnect()) {
            ExecutorService pool = Executors.newSingleThreadExecutor();
/*            GetDataTask getCurrentWeatherDataTask = new GetDataTask("weather/");
            GetDataTask getForecastDataTask = new GetDataTask("forecast/daily/");
            Future<String> jsonCurrentWeatherData = pool.submit(getCurrentWeatherDataTask);
            Future<String> jsonForecastData = pool.submit(getForecastDataTask);
            try {
                JsonParserTask jsonParserTask = new JsonParserTask(jsonCurrentWeatherData.get());
                Future<WeatherData> jsonParcer = pool.submit(jsonParserTask);
                WeatherData weatherData = jsonParcer.get();
                DBWriterTask dbWriterTask = new DBWriterTask(weatherData);
                pool.submit(dbWriterTask);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }*/
            ReceivingDataTask receivingDataTask = new ReceivingDataTask();
            pool.submit(receivingDataTask);
            pool.shutdown();
        }
    }

    private boolean isConnect(){
        ConnectivityManager connectivityManager =
                (ConnectivityManager) MyApp
                        .getAppContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static Context getAppContext() {
        return MyApp.sContext;
    }
}

