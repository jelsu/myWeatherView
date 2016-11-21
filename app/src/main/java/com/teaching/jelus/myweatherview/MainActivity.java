package com.teaching.jelus.myweatherview;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.teaching.jelus.myweatherview.DatabaseHelper.CITY_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.DATETIME_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.IMAGE_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.TABLE_NAME;
import static com.teaching.jelus.myweatherview.DatabaseHelper.TEMPERATURE_COLUMN;
import static com.teaching.jelus.myweatherview.DatabaseHelper.WEATHER_COLUMN;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MyApp";
    private TextView mTemperatureTextView;
    private TextView mShowCityTextView;
    private TextView mWeatherTextView;
    private ImageView mWeatherIconImageView;
    private TextView mDateTimeTextView;
    private DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mShowCityTextView = (TextView) findViewById(R.id.cityTextView);
        mTemperatureTextView = (TextView) findViewById(R.id.temperatureTextView);
        mWeatherTextView = (TextView) findViewById(R.id.weatherTextView);
        mDateTimeTextView = (TextView) findViewById(R.id.dataTextView);
        mWeatherIconImageView = (ImageView) findViewById(R.id.weatherIconImageView);
        //TODO Need to add recyclerView and
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
                    mShowCityTextView.setText(cityName);
                    mTemperatureTextView.setText(String.valueOf(temperature) + "°C");
                    mWeatherTextView.setText(weatherDescription);
                    mDateTimeTextView.setText(
                            "Последнее обновление: " + date);
                    mWeatherIconImageView.setImageBitmap(image);
                } else {
                    Log.d(TAG, "Database is null");
                }
                Log.d(TAG, "TextView fill completed");
                mDatabaseHelper.close();
            }
        });
    }

    private Bitmap convertByteArrayToBitmap(byte[] data){
        Bitmap bitmap= BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;
    }

    private Date convertUnixTimeToData(long unixTime){
        return new Date(unixTime * 1000);
    }
}
