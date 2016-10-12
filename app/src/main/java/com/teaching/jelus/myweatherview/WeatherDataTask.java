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
            JSONObject weatherData = (JSONObject) weatherArray.get(0);
            String sky = weatherData.getString("description");
            JSONObject main = jsonObject.getJSONObject("main");
            String cityName = jsonObject.getString("name");
            double temperature = main.getDouble("temp");
            MainActivity.showCityTextView.setText("City: " + cityName);
            MainActivity.temperatureTextView.setText("Temperature: "
                    + String.format("%.0f", temperature)
                    + "°C");
            MainActivity.weatherTextView.setText("Weather: " + sky);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
