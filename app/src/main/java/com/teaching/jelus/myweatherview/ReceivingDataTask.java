package com.teaching.jelus.myweatherview;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReceivingDataTask implements Runnable {
    private static final String TAG = ReceivingDataTask.class.getSimpleName();
    private final String BEGINNING_URL = "http://api.openweathermap.org/data/2.5/";
    private final String APP_ID = "98fb5e0dcef9e5de3219365edf223805";
    private LockationHelper mLockationHelper;
    private DatabaseHelper mDatabaseHelper;

    @Override
    public void run() {
        try {
            Looper.prepare();
            mDatabaseHelper = new DatabaseHelper(MyApp.getAppContext());
            mLockationHelper = new LockationHelper(MyApp.getAppContext());
            String currentWeatherStringData = getDataOnRequest("weather/");
            String forecastStringData = getDataOnRequest("forecast/daily/");
            JSONObject currentWeatherJsonData = new JSONObject(currentWeatherStringData);
            JSONObject forecastJsonData = new JSONObject(forecastStringData);
            mDatabaseHelper.deleteAll();
            currentWeatherDataInDatabase(currentWeatherJsonData);
            forecastInDatabase(forecastJsonData);
            mDatabaseHelper.showDataInLog();
            mDatabaseHelper.close();
        } catch (Exception e) {
            e.printStackTrace();
            mDatabaseHelper.close();
        }
    }

    private String getDataOnRequest(String requestType) throws Exception {
        double latitude = mLockationHelper.getLatitude();
        double longitude = mLockationHelper.getLongitude();
        Log.d("MyApp", "Current lockation latitude: " + latitude + "; longitude: " + longitude);
        StringBuilder compositeUrl = new StringBuilder(BEGINNING_URL + requestType);
        compositeUrl.append("?lat=" + latitude);
        compositeUrl.append("&lon=" + longitude);
        compositeUrl.append("&lang=ru");
        compositeUrl.append("&appid=" + APP_ID);
        compositeUrl.append("&units=metric");
        Log.d(TAG, "Composite URL: " + compositeUrl.toString());
        URL url = new URL(compositeUrl.toString());
        String result = getStringFromUrl(url);
        Log.d(TAG, "this method with request type " + requestType + " worked");
        return result;
    }

    private void currentWeatherDataInDatabase(JSONObject jsonObject) throws Exception{
        String cityName = jsonObject.getString("name");
        JSONObject main = jsonObject.getJSONObject("main");
        int temperatureMin = (int) Math.round(main.getDouble("temp_min"));
        int temperatureMax = (int) Math.round(main.getDouble("temp_max"));
        JSONArray weatherArray = jsonObject.getJSONArray("weather");
        JSONObject weather = (JSONObject) weatherArray.get(0);
        String weatherDescription = weather.getString("description");
        long dateTime = jsonObject.getLong("dt");
        String iconCode = weather.getString("icon");
        byte[] image = downloadImage(iconCode);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.CITY_COLUMN, cityName);
        values.put(DatabaseHelper.TEMPERATURE_MIN_COLUMN, temperatureMin);
        values.put(DatabaseHelper.TEMPERATURE_MAX_COLUMN, temperatureMax);
        values.put(DatabaseHelper.WEATHER_COLUMN, weatherDescription);
        values.put(DatabaseHelper.DATETIME_COLUMN, dateTime);
        values.put(DatabaseHelper.IMAGE_COLUMN, image);
        //TODO Need add update DB method and entry checked
        db.insert(DatabaseHelper.TABLE_NAME, null, values);
        Log.d(TAG, "currentWeatherDataInDatabase method worked");
    }

    private void forecastInDatabase(JSONObject jsonObject) throws Exception{
        JSONArray list = jsonObject.getJSONArray("list");
        for (int i = 0; i < list.length(); i++)
        {
            JSONObject city = jsonObject.getJSONObject("city");
            String cityName = city.getString("name");
            JSONObject item = (JSONObject) list.get(i);
            JSONObject temp = item.getJSONObject("temp");
            int temperatureMin = (int) Math.round(temp.getDouble("min"));
            int temperatureMax = (int) Math.round(temp.getDouble("max"));
            JSONArray weatherArray = item.getJSONArray("weather");
            JSONObject weather = (JSONObject) weatherArray.get(0);
            String weatherDescription = weather.getString("description");
            long dateTime = item.getLong("dt");
            String iconCode = weather.getString("icon");
            byte[] image = downloadImage(iconCode);
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.CITY_COLUMN, cityName);
            values.put(DatabaseHelper.TEMPERATURE_MIN_COLUMN, temperatureMin);
            values.put(DatabaseHelper.TEMPERATURE_MAX_COLUMN, temperatureMax);
            values.put(DatabaseHelper.WEATHER_COLUMN, weatherDescription);
            values.put(DatabaseHelper.DATETIME_COLUMN, dateTime);
            values.put(DatabaseHelper.IMAGE_COLUMN, image);
            //TODO Need add update DB method and entry checked
            db.insert(DatabaseHelper.TABLE_NAME, null, values);
            //db.update(DatabaseHelper.TABLE_NAME, values, BaseColumns._ID + " = 2", null);
        }
        Log.d(TAG, "forecastInDatabase method worked");
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
