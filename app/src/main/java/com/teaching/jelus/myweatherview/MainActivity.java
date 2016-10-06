package com.teaching.jelus.myweatherview;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private String url = "http://api.openweathermap.org/data/2.5/forecast/";
    private String cityId = "498817";
    private String appId = "98fb5e0dcef9e5de3219365edf223805";
    private TextView temperatureTextView;
    private TextView showCityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showCityTextView = (TextView) findViewById(R.id.cityTextView);
        temperatureTextView = (TextView) findViewById(R.id.temperatureTextView);
        new WeatherDataTask().execute(url + "city?id=" + cityId + "&APPID=" + appId);
    }

    class WeatherDataTask extends AsyncTask<String, Void, String> {
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
                JSONObject cityObj = jsonObject.getJSONObject("city");
                JSONArray weatherDataArray = jsonObject.getJSONArray("list");
                JSONObject JSONObjectWeatherData = (JSONObject) weatherDataArray.get(0);
                JSONObject JSONObjectTemperature = JSONObjectWeatherData.getJSONObject("main");
                String name = cityObj.getString("name");
                double temperature = JSONObjectTemperature.getDouble("temp") - 273;
                showCityTextView.setText("City: " + name);
                temperatureTextView.setText("Temperature: "
                        + String.format("%.0f", temperature)
                        + "°C");
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
