package com.teaching.jelus.myweatherview.fragments;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teaching.jelus.myweatherview.DataEvent;
import com.teaching.jelus.myweatherview.ForecastData;
import com.teaching.jelus.myweatherview.R;
import com.teaching.jelus.myweatherview.adapters.RecyclerAdapter;
import com.teaching.jelus.myweatherview.helpers.DatabaseHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.teaching.jelus.myweatherview.helpers.DatabaseHelper.CITY_COLUMN;
import static com.teaching.jelus.myweatherview.helpers.DatabaseHelper.DATE_COLUMN;
import static com.teaching.jelus.myweatherview.helpers.DatabaseHelper.DESCRIPTION_COLUMN;
import static com.teaching.jelus.myweatherview.helpers.DatabaseHelper.IMAGE_COLUMN;
import static com.teaching.jelus.myweatherview.helpers.DatabaseHelper.TABLE_NAME;
import static com.teaching.jelus.myweatherview.helpers.DatabaseHelper.TEMPERATURE_MAX_COLUMN;
import static com.teaching.jelus.myweatherview.helpers.DatabaseHelper.TEMPERATURE_MIN_COLUMN;

@SuppressWarnings("WrongConstant")
public class WeatherFragment extends Fragment {
    private static final String TAG = WeatherFragment.class.getSimpleName();
    private TextView mTempTextView;
    private TextView mCityNameTextView;
    private TextView mDescriptionTextView;
    private ImageView mWeatherImageView;
    private RecyclerView mRecyclerView;
    private TextView mDateTimeTextView;
    private DatabaseHelper mDatabaseHelper;
    private FrameLayout mProgressFragment;
    private RelativeLayout mDataFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        mProgressFragment = (FrameLayout) view.findViewById(R.id.fragment_progress);
        mDataFragment = (RelativeLayout) view.findViewById(R.id.fragment_data);
        mCityNameTextView = (TextView) view.findViewById(R.id.text_city_name);
        mTempTextView = (TextView) view.findViewById(R.id.text_temperature);
        mDescriptionTextView = (TextView) view.findViewById(R.id.text_weather_description);
        mDateTimeTextView = (TextView) view.findViewById(R.id.text_weather_date);
        mWeatherImageView = (ImageView) view.findViewById(R.id.image_weather);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_forecast);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        return view;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void updateUI(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO realize update via handler
                mDatabaseHelper = new DatabaseHelper(getActivity());
                SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
                updateCurrWeatherWidgets(db);
                updateForecastWidgets(db);
                mDatabaseHelper.close();
                Log.d(TAG, "update UI completed");
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataEvent data) {
        switch (data.getType()) {
            case RECEIVE_DATA:
                updateUI();
                mProgressFragment.setVisibility(View.GONE);
                mDataFragment.setVisibility(View.VISIBLE);
                break;
            case UPDATE_DATA:
                mProgressFragment.setVisibility(View.VISIBLE);
                mDataFragment.setVisibility(View.GONE);
                break;
            case BACK:
                updateUI();
                mProgressFragment.setVisibility(View.GONE);
                mDataFragment.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateCurrWeatherWidgets(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()){
            int cityColIndex = cursor.getColumnIndex(CITY_COLUMN);
            int tempMinColIndex = cursor.getColumnIndex(TEMPERATURE_MIN_COLUMN);
            int tempMaxColIndex = cursor.getColumnIndex(TEMPERATURE_MAX_COLUMN);
            int descriptionColIndex = cursor.getColumnIndex(DESCRIPTION_COLUMN);
            int dateColIndex = cursor.getColumnIndex(DATE_COLUMN);
            int imageColIndex = cursor.getColumnIndex(IMAGE_COLUMN);
            String cityName = cursor.getString(cityColIndex);
            int tempMin = cursor.getInt(tempMinColIndex);
            int tempMax = cursor.getInt(tempMaxColIndex);
            String averageTemp = String.valueOf(Math.round((double) (tempMax
                    + tempMin) / 2));
            String description = cursor.getString(descriptionColIndex);
            long unixDate = cursor.getLong(dateColIndex);
            long lastUpdateTime = getDifferenceBetweenDates(unixDate * 1000);
            String lastUpdateStr = getLastUpdateString(lastUpdateTime);
            Bitmap image = convertByteArrayToBitmap(cursor.getBlob(imageColIndex));
            mCityNameTextView.setText(cityName);
            mTempTextView.setText(averageTemp + "°");
            mDescriptionTextView.setText(description);
            mDateTimeTextView.setText(lastUpdateStr);
            mWeatherImageView.setImageBitmap(image);
        } else {
            Log.d(TAG, "Database is null");
        }
        cursor.close();
    }

    private void updateForecastWidgets(SQLiteDatabase db) {
        ArrayList<ForecastData> forecastArrayList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()){
            cursor.moveToNext();
            do {
                int tempMinColIndex = cursor.getColumnIndex(TEMPERATURE_MIN_COLUMN);
                int tempMaxColIndex = cursor.getColumnIndex(TEMPERATURE_MAX_COLUMN);
                int descriptionColIndex = cursor.getColumnIndex(DESCRIPTION_COLUMN);
                int dateColIndex = cursor.getColumnIndex(DATE_COLUMN);
                int imageColIndex = cursor.getColumnIndex(IMAGE_COLUMN);
                int tempMin = cursor.getInt(tempMinColIndex);
                int tempMax = cursor.getInt(tempMaxColIndex);
                String description = cursor.getString(descriptionColIndex);
                String date = getStringDate(convertUnixTimeToDate(cursor.getLong(dateColIndex)));
                Bitmap image = convertByteArrayToBitmap(cursor.getBlob(imageColIndex));
                ForecastData forecastData = new ForecastData(tempMin,
                        tempMax,
                        description,
                        date,
                        image);
                forecastArrayList.add(forecastData);
            }
            while (cursor.moveToNext());
        } else {
            Log.d(TAG, "Database is null");
        }
        cursor.close();
        RecyclerView.Adapter adapter = new RecyclerAdapter(forecastArrayList);
        mRecyclerView.setAdapter(adapter);
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
