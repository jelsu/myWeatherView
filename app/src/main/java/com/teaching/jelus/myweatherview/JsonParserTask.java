package com.teaching.jelus.myweatherview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.Callable;

public class JsonParserTask implements Callable<WeatherData> {
    private final String TAG = "MyApp";
    private JSONObject mJSONObject;

    public JsonParserTask(String JSONString) {
        try {
            mJSONObject = new JSONObject(JSONString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public WeatherData call() throws Exception {
        JSONArray weatherArray = mJSONObject.getJSONArray("weather");
        JSONObject weather = (JSONObject) weatherArray.get(0);
        String description = weather.getString("description");
        String iconCode = weather.getString("icon");
        byte[] image = downloadImage(iconCode);
        JSONObject main = mJSONObject.getJSONObject("main");
        int temperature = (int) Math.round(main.getDouble("temp"));
        String cityName = mJSONObject.getString("name");
        Date time = new Date((long) 1478941200 * 1000);
        Log.d(TAG, "Date of last access: " + time.toString());
        Log.d(TAG, "JsonParserTask completed");
        return new WeatherData(description, temperature, cityName, image);
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
