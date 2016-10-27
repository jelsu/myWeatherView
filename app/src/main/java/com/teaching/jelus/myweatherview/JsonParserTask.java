package com.teaching.jelus.myweatherview;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

public class JsonParserTask implements Callable<WeatherData> {
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
        JSONObject main = mJSONObject.getJSONObject("main");
        int temperature = (int) Math.round(main.getDouble("temp"));
        String cityName = mJSONObject.getString("name");
        Log.d("MyApp", "JsonParserTask completed");
        return new WeatherData(description, temperature, cityName, iconCode);
    }
}
