package com.teaching.jelus.myweatherview;


import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class GetDataTask implements Callable<String> {
    private final String APP_ID = "98fb5e0dcef9e5de3219365edf223805";
    private final String LOCATION_ID = "498817";
    private final String CURRENT_CONDITIONS_URL = "http://api.openweathermap.org/data/2.5/weather/";
    private String compositeURL;
    private HttpURLConnection urlConnection = null;
    private BufferedReader reader  = null;
    private InputStream inputStream = null;
    private StringBuffer buffer = null;
    private String result;

    @Override
    public String call() throws Exception {
        compositeURL = CURRENT_CONDITIONS_URL
                + "?id=" + LOCATION_ID
                + "&appid=" + APP_ID
                + "&units=metric";
        try {
            URL url = new URL(compositeURL);
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
            result = buffer.toString();
        } catch (Exception e){
            e.printStackTrace();
        }
        Log.d("MyApp", "GetDataTask completed");
        return result;
    }
}
