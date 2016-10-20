package com.teaching.jelus.myweatherview;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

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
        mDatabaseHelper = new DatabaseHelper(MyApp.getAppContext());
        if (isConnect()) {
            try {
                JSONObject jsonObject = new JSONObject(JSONdata);
                JSONArray weatherArray = jsonObject.getJSONArray("weather");
                JSONObject allWeatherData = (JSONObject) weatherArray.get(0);
                String currentWeather = allWeatherData.getString("description");
                String iconCode = allWeatherData.getString("icon");
                JSONObject main = jsonObject.getJSONObject("main");
                String cityName = jsonObject.getString("name");
                int temperature = (int) Math.round(main.getDouble("temp"));
                mDatabaseHelper.deleteAll();
                mDatabaseHelper.addWeatherData(cityName, temperature, currentWeather, iconCode);
                WeatherData weatherData = mDatabaseHelper.getWeatherData();
                MainActivity.showCityTextView.setText(weatherData.getCity());
                MainActivity.temperatureTextView.setText(String.valueOf(weatherData.getTemperature())
                        + "°C");
                MainActivity.weatherTextView.setText(weatherData.getWeather());
                new DownloadImageTask().execute(weatherData.getIconCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else{
            WeatherData weatherData = mDatabaseHelper.getWeatherData();
            MainActivity.showCityTextView.setText(weatherData.getCity());
            MainActivity.temperatureTextView.setText(String.valueOf(weatherData.getTemperature())
                    + "°C");
            MainActivity.weatherTextView.setText(weatherData.getWeather());
            new DownloadImageTask().execute(weatherData.getIconCode());
        }
        Toast.makeText(
                MyApp.getAppContext(),
                "Connect: " + isConnect(),
                Toast.LENGTH_SHORT)
                .show();
    }

    public boolean isConnect(){
        ConnectivityManager connectivityManager =
                (ConnectivityManager) MyApp
                        .getAppContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
