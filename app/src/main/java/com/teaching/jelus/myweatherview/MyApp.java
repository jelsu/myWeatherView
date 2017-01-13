package com.teaching.jelus.myweatherview;

import android.app.Application;

import com.teaching.jelus.myweatherview.helper.DatabaseHelper;
import com.teaching.jelus.myweatherview.helper.LocationHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApp extends Application {
    private static ExecutorService sPool;
    private static Settings sSettings;
    private static DatabaseHelper sDatabaseHelper;
    private static LocationHelper sLocationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        sPool = Executors.newCachedThreadPool();
        sSettings = new Settings(getApplicationContext());
        sDatabaseHelper = new DatabaseHelper(getApplicationContext());
        sLocationHelper = new LocationHelper(getApplicationContext());
    }

    public static ExecutorService getPool() {
        return sPool;
    }

    public static Settings getSettings() {
        return sSettings;
    }

    public static DatabaseHelper getDatabaseHelper() {
        return sDatabaseHelper;
    }

    public static LocationHelper getLocationHelper() {
        return sLocationHelper;
    }
}

