package com.teaching.jelus.myweatherview;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherDataTask extends AsyncTask<String, Void, String> {
    private HttpURLConnection urlConnection = null;
    private BufferedReader reader = null;
    private String result = "no data";
    private DatabaseHelper mDatabaseHelper;

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null){
                buffer.append(line);
            }
            result = buffer.toString();
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String JSONdata) {
        super.onPostExecute(JSONdata);
        try {
            JSONObject jsonObject = new JSONObject(JSONdata);
            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            JSONObject allWeatherData = (JSONObject) weatherArray.get(0);
            String currentWeather = allWeatherData.getString("description");
            JSONObject main = jsonObject.getJSONObject("main");
            String cityName = jsonObject.getString("name");
            int temperature = (int) Math.round(main.getDouble("temp"));
            mDatabaseHelper = new DatabaseHelper(MyApp.getAppContext());
            mDatabaseHelper.addWeatherData(cityName, temperature, currentWeather);
            WeatherData weatherData = mDatabaseHelper.getWeatherData();
            MainActivity.showCityTextView.setText("City: " + weatherData.getCity());
            MainActivity.temperatureTextView.setText("Temperature: "
                    + String.valueOf(weatherData.getTemperature())
                    + "°C");
            MainActivity.weatherTextView.setText("Weather: " + weatherData.getWeather());
            mDatabaseHelper.deleteAll();
        } catch (Exception e){
            e.printStackTrace();
            mDatabaseHelper.deleteAll();
        }
    }
}
