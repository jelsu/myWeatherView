package com.teaching.jelus.myweatherview;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.teaching.jelus.myweatherview.DatabaseHelper.CITY_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.DATETIME_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.IMAGE_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.TABLE_NAME;
import static com.teaching.jelus.myweatherview.DatabaseHelper.TEMPERATURE_MAX_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.TEMPERATURE_MIN_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.WEATHER_COLUMN;

public class WeatherFragment extends Fragment {
    private static final String TAG = WeatherFragment.class.getSimpleName();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        mCityNameTextView = (TextView) view.findViewById(R.id.text_city_name);
        mTemperatureTextView = (TextView) view.findViewById(R.id.text_temperature);
        mWeatherDescriptionTextView = (TextView) view.findViewById(R.id.text_weather_description);
        mDateTimeTextView = (TextView) view.findViewById(R.id.text_weather_date);
        mWeatherImageView = (ImageView) view.findViewById(R.id.image_weather);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_forecast);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        Log.d(TAG, "onCreateView completed");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI();
    }

    private void updateUI(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDatabaseHelper = new DatabaseHelper(getActivity());
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
            long lastUpdateTime = getDifferenceBetweenDates(unixDate * 1000);
            String lastUpdateStr = getLastUpdateString(lastUpdateTime);
            Bitmap image = convertByteArrayToBitmap(cursor.getBlob(imageColIndex));
            mCityNameTextView.setText(cityName);
            mTemperatureTextView.setText(averageTemperature + "°");
            mWeatherDescriptionTextView.setText(weatherDescription);
            mDateTimeTextView.setText(lastUpdateStr);
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
                String date = getStringDate(convertUnixTimeToDate(cursor.getLong(dateTimeColIndex)));
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

    private Date convertUnixTimeToDate(long unixTime){
        return new Date(unixTime * 1000);
    }

    private String getStringDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM");
        Date currentDate = new Date();
        long minDifference = getDifferenceBetweenDates(date.getTime()) - 720;
        if (dateFormat.format(date).equals(dateFormat.format(currentDate))){
            return "Today";
        } else if (minDifference <= 1440){
            return "Tomorrow";
        }
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date);
    }

    private long getDifferenceBetweenDates(long date) {
        long millisDifference = Math.abs(System.currentTimeMillis() - date) / 1000;
        return millisDifference / 60;
    }

    private String getLastUpdateString(long updateTime){
        if (updateTime >= 60){
            int hours = (int) updateTime / 60;
            if (hours > 24){
                return "Updated a few days ago";
            }
            return "Updated " + hours + " hours ago";
        }
        return "Updated " + updateTime + " minutes ago";
    }
}
