package com.teaching.jelus.myweatherview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static TextView temperatureTextView;
    public static TextView showCityTextView;
    public static TextView weatherTextView;
    public static ImageView weatherIconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showCityTextView = (TextView) findViewById(R.id.cityTextView);
        temperatureTextView = (TextView) findViewById(R.id.temperatureTextView);
        weatherTextView = (TextView) findViewById(R.id.weatherTextView);
        weatherIconImageView = (ImageView) findViewById(R.id.weatherIconImageView);
    }

}
