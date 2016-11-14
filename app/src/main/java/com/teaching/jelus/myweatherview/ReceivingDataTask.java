package com.teaching.jelus.myweatherview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class ReceivingDataTask implements Runnable {
    private final String TAG = "MyApp";
    private final String BEGINNING_URL = "http://api.openweathermap.org/data/2.5/";
    private final String APP_ID = "98fb5e0dcef9e5de3219365edf223805";
    private LockationHelper lockationHelper;

    @Override
    public void run() {
        try {
            Looper.prepare();
            lockationHelper = new LockationHelper(MyApp.getAppContext());
            String currentWeatherStringData = getDataOnRequest("weather/");
            String forecastStringData = getDataOnRequest("forecast/daily/");
            JSONObject currentWeatherJsonData = new JSONObject(currentWeatherStringData);
            JSONObject forecastJsonData = new JSONObject(forecastStringData);
            WeatherData weatherData = fillWeatherData(currentWeatherJsonData);
            fillDatabase(weatherData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDataOnRequest(String requestType) throws Exception {
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
        Log.d(TAG, "getDataOnRequest method with request type " + requestType + " worked");
        return result;
    }

    private WeatherData fillWeatherData(JSONObject jsonObject) throws Exception {
        JSONArray weatherArray = jsonObject.getJSONArray("weather");
        JSONObject weather = (JSONObject) weatherArray.get(0);
        String description = weather.getString("description");
        String iconCode = weather.getString("icon");
        byte[] image = downloadImage(iconCode);
        JSONObject main = jsonObject.getJSONObject("main");
        int temperature = (int) Math.round(main.getDouble("temp"));
        String cityName = jsonObject.getString("name");
        Date date = new Date(jsonObject.getLong("dt") * 1000);
        Log.d(TAG, "fillWeatherData method worked");
        EventBus.getDefault().post(date);
        return new WeatherData(description, temperature, cityName, image);
    }

    private void fillDatabase (WeatherData weatherData) {
        DatabaseHelper mDatabaseHelper;
        mDatabaseHelper = new DatabaseHelper(MyApp.getAppContext());
        try {
            mDatabaseHelper.deleteAll();
            mDatabaseHelper.addWeatherData(
                    weatherData.getCityName(),
                    weatherData.getTemperature(),
                    weatherData.getWeatherDescription(),
                    weatherData.getIcon());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "fillDatabase method worked");
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

    private byte[] downloadImage(String icon){
        String url = "http://openweathermap.org/img/w/" + icon + ".png";
        try {
            InputStream in = new java.net.URL(url).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            byte[] buffer = out.toByteArray();
            return buffer;
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return new byte[0];
    }
}
