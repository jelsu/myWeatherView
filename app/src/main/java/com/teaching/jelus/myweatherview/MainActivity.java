package com.teaching.jelus.myweatherview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
        WeatherData weatherData = mDatabaseHelper.getWeatherData();
        showCityTextView.setText(weatherData.getCityName());
        temperatureTextView.setText(String.valueOf(weatherData.getTemperature())
                + "°C");
        weatherTextView.setText(weatherData.getWeatherDescription());
        Bitmap image = convertByteArrayToBitmap(weatherData.getIcon());
        weatherIconImageView.setImageBitmap(image);
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

    private Bitmap convertByteArrayToBitmap(byte[] data){
        Bitmap bitmap= BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;
    }

}
