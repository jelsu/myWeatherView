package com.teaching.jelus.myweatherview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MyApp";
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
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDatabaseHelper = new DatabaseHelper(MyApp.getAppContext());
                WeatherData weatherData = mDatabaseHelper.getWeatherData();
                showCityTextView.setText(weatherData.getCityName());
                temperatureTextView.setText(String.valueOf(weatherData.getTemperature())
                        + "°C");
                weatherTextView.setText(weatherData.getWeatherDescription());
                Bitmap image = convertByteArrayToBitmap(weatherData.getIcon());
                weatherIconImageView.setImageBitmap(image);
                Log.d(TAG, "TextView fill completed");
                mDatabaseHelper.close();
            }
        });
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();

    }

    @Subscribe
    public void onEventMainThread(Date event){
        Log.d(TAG, "Date of last access: " + event);
    }

    private Bitmap convertByteArrayToBitmap(byte[] data){
        Bitmap bitmap= BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;
    }

}
