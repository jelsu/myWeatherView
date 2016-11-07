package com.teaching.jelus.myweatherview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {
    private static final String DATABASE_NAME = "mydatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "weather";
    private static final String CITY_COLUMN = "city";
    private static final String TEMPERATURE_COLUMN = "temperature";
    private static final String WEATHER_COLUMN = "weather";
    private static final String IMAGE_COLUMN = "icon";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "create table "
                + TABLE_NAME
                + " (" + BaseColumns._ID + " integer primary key autoincrement, "
                + CITY_COLUMN + " text not null, "
                + TEMPERATURE_COLUMN + " integer not null, "
                + WEATHER_COLUMN + " text not null,"
                + IMAGE_COLUMN + " blob not null);";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("SQLite", "Update " + oldVersion + " version to " + newVersion + "version");
        db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_NAME);
        onCreate(db);
    }

    public void addWeatherData(
            String cityName,
            int temperature,
            String weather,
            byte[] image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CITY_COLUMN, cityName);
        values.put(TEMPERATURE_COLUMN, temperature);
        values.put(WEATHER_COLUMN, weather);
        values.put(IMAGE_COLUMN, image);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public WeatherData getWeatherData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{CITY_COLUMN,
                        TEMPERATURE_COLUMN,
                        WEATHER_COLUMN,
                        IMAGE_COLUMN},
                null, null, null, null, null);
        cursor.moveToFirst();
        String cityDB = cursor.getString(cursor.getColumnIndex(CITY_COLUMN));
        int temperature = cursor.getInt(cursor.getColumnIndex(TEMPERATURE_COLUMN));
        String weather = cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN));
        byte[] image = cursor.getBlob(cursor.getColumnIndex(IMAGE_COLUMN));
        cursor.close();
        WeatherData weatherData = new WeatherData(cityDB, temperature, weather, image);
        return weatherData;
    }


    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}
