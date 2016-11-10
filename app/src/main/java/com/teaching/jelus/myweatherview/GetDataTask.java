package com.teaching.jelus.myweatherview;


import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class GetDataTask implements Callable<String> {
    private final String TAG = "MyApp";
    private final String BEGINNING_URL = "http://api.openweathermap.org/data/2.5/";
    private final String APP_ID = "98fb5e0dcef9e5de3219365edf223805";
    private String requestType;

    public GetDataTask(String requestType) {
        this.requestType = requestType;
    }

    @Override
    public String call() throws Exception {
        Looper.prepare();
        LockationHelper lockationHelper = new LockationHelper(MyApp.getAppContext());
        double latitude = lockationHelper.getLatitude();
        double longitude = lockationHelper.getLongitude();
        Log.d("MyApp", "Current lockation latitude: " + latitude + "; longitude: " + longitude);
        StringBuilder compositeURL = new StringBuilder(BEGINNING_URL + requestType);
        compositeURL.append("?lat=" + latitude);
        compositeURL.append("&lon=" + longitude);
        compositeURL.append("&lang=ru");
        compositeURL.append("&appid=" + APP_ID);
        compositeURL.append("&units=metric");
        Log.d(TAG, "Composite URL: " + compositeURL.toString());
        URL url = new URL(compositeURL.toString());
        String result = getStringFromUrl(url);
        Log.d(TAG, "GetDataTask with request type " + requestType + " completed");
        return result;
    }

    private String getStringFromUrl(URL url) throws IOException {
        HttpURLConnection urlConnection;
        BufferedReader reader;
        InputStream inputStream;
        StringBuffer buffer;
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        inputStream = urlConnection.getInputStream();
        buffer = new StringBuffer();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null){
            buffer.append(line);
        }
        return buffer.toString();
    }
}
