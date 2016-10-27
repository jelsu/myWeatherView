package com.teaching.jelus.myweatherview;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static TextView temperatureTextView;
    public static TextView showCityTextView;
    public static TextView weatherTextView;
    public static ImageView weatherIconImageView;
    DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showCityTextView = (TextView) findViewById(R.id.cityTextView);
        temperatureTextView = (TextView) findViewById(R.id.temperatureTextView);
        weatherTextView = (TextView) findViewById(R.id.weatherTextView);
        weatherIconImageView = (ImageView) findViewById(R.id.weatherIconImageView);
        mDatabaseHelper = new DatabaseHelper(MyApp.getAppContext());
        if (isConnect()) {
            try {
                WeatherData weatherData = mDatabaseHelper.getWeatherData();
                MainActivity.showCityTextView.setText(weatherData.getCityName());
                MainActivity.temperatureTextView.setText(String.valueOf(weatherData.getTemperature())
                        + "°C");
                MainActivity.weatherTextView.setText(weatherData.getWeatherDescription());
                new DownloadImageTask().execute(weatherData.getIconCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            WeatherData weatherData = mDatabaseHelper.getWeatherData();
            MainActivity.showCityTextView.setText(weatherData.getCityName());
            MainActivity.temperatureTextView.setText(String.valueOf(weatherData.getTemperature())
                    + "°C");
            MainActivity.weatherTextView.setText(weatherData.getWeatherDescription());
            new DownloadImageTask().execute(weatherData.getIconCode());
        }
        Log.d("MyApp", "TextView fill completed");
        Toast.makeText(getApplicationContext(), "Connect: " + isConnect(), Toast.LENGTH_SHORT).show();
        mDatabaseHelper.close();
    }

    private boolean isConnect(){
        ConnectivityManager connectivityManager =
                (ConnectivityManager) MyApp
                        .getAppContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

}
