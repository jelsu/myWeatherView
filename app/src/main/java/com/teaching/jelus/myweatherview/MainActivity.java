package com.teaching.jelus.myweatherview;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.teaching.jelus.myweatherview.DatabaseHelper.CITY_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.DATETIME_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.IMAGE_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.TABLE_NAME;
import static com.teaching.jelus.myweatherview.DatabaseHelper.TEMPERATURE_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.WEATHER_COLUMN;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MyApp";
    private TextView mCurrentWeatherTemperatureTextView;
    private TextView mCurrentWeatherCityTextView;
    private TextView mCurrentWeatherDescriptionTextView;
    private ImageView mCurrentWeatherImageView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView mDateTimeTextView;
    private DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCurrentWeatherCityTextView = (TextView) findViewById(R.id.currentWeatherCityTextView);
        mCurrentWeatherTemperatureTextView = (TextView) findViewById(R.id.currentWeatherTemperatureTextView);
        mCurrentWeatherDescriptionTextView = (TextView) findViewById(R.id.currentWeatherDescriptionTextView);
        mDateTimeTextView = (TextView) findViewById(R.id.currentWeatherDataTextView);
        mCurrentWeatherImageView = (ImageView) findViewById(R.id.currentWeatherImageView);
        mRecyclerView = (RecyclerView) findViewById(R.id.forecastRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }

    private void updateUI(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO This is temporary solution, needs correction
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mDatabaseHelper = new DatabaseHelper(MyApp.getAppContext());
                SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
                showCurrentWeatherData(db);
                showForecastData(db);
                Log.d(TAG, "UI fill completed");
                mDatabaseHelper.close();
            }
        });
    }

    private void showCurrentWeatherData(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()){
            int cityColIndex = cursor.getColumnIndex(CITY_COLUMN);
            int temperatureColIndex = cursor.getColumnIndex(TEMPERATURE_COLUMN);
            int weatherColIndex = cursor.getColumnIndex(WEATHER_COLUMN);
            int dateTimeColIndex = cursor.getColumnIndex(DATETIME_COLUMN);
            int imageColIndex = cursor.getColumnIndex(IMAGE_COLUMN);
            String cityName = cursor.getString(cityColIndex);
            int temperature = cursor.getInt(temperatureColIndex);
            String weatherDescription = cursor.getString(weatherColIndex);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
            String date = dateFormat
                    .format(convertUnixTimeToData(cursor.getLong(dateTimeColIndex)));
            Bitmap image = convertByteArrayToBitmap(cursor.getBlob(imageColIndex));
            mCurrentWeatherCityTextView.setText(cityName);
            mCurrentWeatherTemperatureTextView.setText(String.valueOf(temperature) + "°C");
            mCurrentWeatherDescriptionTextView.setText(weatherDescription);
            mDateTimeTextView.setText(
                    "Последнее обновление: " + date);
            mCurrentWeatherImageView.setImageBitmap(image);
        } else {
            Log.d(TAG, "Database is null");
        }
        cursor.close();
    }

    private void showForecastData(SQLiteDatabase db) {
        ArrayList<ForecastData> forecastArrayList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()){
            cursor.moveToNext();
            do {
                int cityColIndex = cursor.getColumnIndex(CITY_COLUMN);
                int temperatureColIndex = cursor.getColumnIndex(TEMPERATURE_COLUMN);
                int weatherColIndex = cursor.getColumnIndex(WEATHER_COLUMN);
                int dateTimeColIndex = cursor.getColumnIndex(DATETIME_COLUMN);
                int imageColIndex = cursor.getColumnIndex(IMAGE_COLUMN);
                String cityName = cursor.getString(cityColIndex);
                int temperature = cursor.getInt(temperatureColIndex);
                String weatherDescription = cursor.getString(weatherColIndex);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
                String date = dateFormat
                        .format(convertUnixTimeToData(cursor.getLong(dateTimeColIndex)));
                Bitmap image = convertByteArrayToBitmap(cursor.getBlob(imageColIndex));
                ForecastData forecastData =
                        new ForecastData(temperature, cityName, weatherDescription, date, image);
                forecastArrayList.add(forecastData);
            }
            while (cursor.moveToNext());
        } else {
            Log.d(TAG, "Database is null");
        }
        cursor.close();
        mAdapter = new RecyclerAdapter(forecastArrayList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private Bitmap convertByteArrayToBitmap(byte[] data){
        Bitmap bitmap= BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;
    }

    private Date convertUnixTimeToData(long unixTime){
        return new Date(unixTime * 1000);
    }
}
