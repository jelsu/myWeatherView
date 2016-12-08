package com.teaching.jelus.myweatherview;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApp extends Application {
    private static ExecutorService sPool;

    @Override
    public void onCreate() {
        super.onCreate();
        sPool = Executors.newCachedThreadPool();
    }

    public static ExecutorService getPool() {
        return sPool;
    }
}

