package com.teaching.jelus.myweatherview;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApp extends Application {
    private static ExecutorService sPool;
    private static Settings sSettings;

    @Override
    public void onCreate() {
        super.onCreate();
        sPool = Executors.newCachedThreadPool();
        sSettings = new Settings(getApplicationContext());
    }

    public static ExecutorService getPool() {
        return sPool;
    }

    public static Settings getSettings() {
        return sSettings;
    }
}

