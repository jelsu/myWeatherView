package com.teaching.jelus.myweatherview;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

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
            ReceivingDataTask receivingDataTask = new ReceivingDataTask();
            pool.submit(receivingDataTask);
            pool.shutdown();
            Toast.makeText(sContext, "isConnect() " + isConnect(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(sContext, "isConnect() " + isConnect(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isConnect(){
        ConnectivityManager connectivityManager = (ConnectivityManager) MyApp
                        .getAppContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static Context getAppContext() {
        return MyApp.sContext;
    }
}

