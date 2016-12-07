package com.teaching.jelus.myweatherview;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

    public static boolean isConnect(){
        ConnectivityManager connectivityManager = (ConnectivityManager) sContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static ExecutorService getPool() {
        return sPool;
    }
}

