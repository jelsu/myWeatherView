package com.teaching.jelus.myweatherview;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApp extends Application {
    private static Context sContext;
    private static ExecutorService sPool;

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.sContext = getApplicationContext();
        sPool = Executors.newSingleThreadExecutor();
    }

    public static Context getAppContext() {
        return MyApp.sContext;
    }

    public static ExecutorService getPool() {
        return sPool;
    }
}

