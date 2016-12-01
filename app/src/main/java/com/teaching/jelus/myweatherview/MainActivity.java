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
import static com.teaching.jelus.myweatherview.DatabaseHelper.TEMPERATURE_MAX_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.TEMPERATURE_MIN_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.WEATHER_COLUMN;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView mTemperatureTextView;
    private TextView mCityNameTextView;
    private TextView mWeatherDescriptionTextView;
    private ImageView mWeatherImageView;
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
        mCityNameTextView = (TextView) findViewById(R.id.text_city_name);
        mTemperatureTextView = (TextView) findViewById(R.id.text_temperature);
        mWeatherDescriptionTextView = (TextView) findViewById(R.id.text_weather_description);
        mDateTimeTextView = (TextView) findViewById(R.id.text_weather_date);
        mWeatherImageView = (ImageView) findViewById(R.id.image_weather);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_forecast);
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
            int temperatureMinColIndex = cursor.getColumnIndex(TEMPERATURE_MIN_COLUMN);
            int temperatureMaxColIndex = cursor.getColumnIndex(TEMPERATURE_MAX_COLUMN);
            int weatherColIndex = cursor.getColumnIndex(WEATHER_COLUMN);
            int dateTimeColIndex = cursor.getColumnIndex(DATETIME_COLUMN);
            int imageColIndex = cursor.getColumnIndex(IMAGE_COLUMN);
            String cityName = cursor.getString(cityColIndex);
            int temperatureMin = cursor.getInt(temperatureMinColIndex);
            int temperatureMax = cursor.getInt(temperatureMaxColIndex);
            String averageTemperature = String.valueOf(Math.round((double) (temperatureMax
                    + temperatureMin) / 2));
            String weatherDescription = cursor.getString(weatherColIndex);
            long unixDate = cursor.getLong(dateTimeColIndex);
            long lastUpdate = getMinDifferenceBetweenDates(unixDate * 1000);
            Bitmap image = convertByteArrayToBitmap(cursor.getBlob(imageColIndex));
            mCityNameTextView.setText(cityName);
            mTemperatureTextView.setText(averageTemperature + "°");
            mWeatherDescriptionTextView.setText(weatherDescription);
            mDateTimeTextView.setText(
                    "Обновлено " + lastUpdate + " мин. назад");
            mWeatherImageView.setImageBitmap(image);
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
                int temperatureMinColIndex = cursor.getColumnIndex(TEMPERATURE_MIN_COLUMN);
                int temperatureMaxColIndex = cursor.getColumnIndex(TEMPERATURE_MAX_COLUMN);
                int weatherColIndex = cursor.getColumnIndex(WEATHER_COLUMN);
                int dateTimeColIndex = cursor.getColumnIndex(DATETIME_COLUMN);
                int imageColIndex = cursor.getColumnIndex(IMAGE_COLUMN);
                int temperatureMin = cursor.getInt(temperatureMinColIndex);
                int temperatureMax = cursor.getInt(temperatureMaxColIndex);
                String weatherDescription = cursor.getString(weatherColIndex);
                String date = getStringDate(convertUnixTimeToData(cursor.getLong(dateTimeColIndex)));
                Bitmap image = convertByteArrayToBitmap(cursor.getBlob(imageColIndex));
                ForecastData forecastData =
                        new ForecastData(temperatureMin, temperatureMax, weatherDescription, date, image);
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

    private String getStringDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM");
        Date currentDate = new Date();
        long minDifference = getMinDifferenceBetweenDates(date.getTime());
        if (dateFormat.format(date).equals(dateFormat.format(currentDate))){
            return "Сегодня";
        } else if (minDifference <= 1440){
            return "Завтра";
        }
        return dateFormat.format(date);
    }

    private long getMinDifferenceBetweenDates(long date) {
        long millisDifference = Math.abs(System.currentTimeMillis() - date) / 1000;
        return millisDifference / 60;
    }
}
