package com.teaching.jelus.myweatherview;

import android.util.Log;

public class DBWriterTask implements Runnable {
    DatabaseHelper mDatabaseHelper;
    WeatherData mWeatherData;

    public DBWriterTask(WeatherData weatherData) {
        mWeatherData = weatherData;
    }

    @Override
    public void run() {
        mDatabaseHelper = new DatabaseHelper(MyApp.getAppContext());
            try {
                mDatabaseHelper.deleteAll();
                mDatabaseHelper.addWeatherData(
                        mWeatherData.getCityName(),
                        mWeatherData.getTemperature(),
                        mWeatherData.getWeatherDescription(),
                        mWeatherData.getIcon());
            } catch (Exception e) {
                e.printStackTrace();
            }
        Log.d("MyApp", "DBWriterTask completed");
    }
}
