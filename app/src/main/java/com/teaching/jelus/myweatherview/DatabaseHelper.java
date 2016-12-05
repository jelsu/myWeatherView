package com.teaching.jelus.myweatherview;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {
    private static final String TAG = DatabaseHelper.class.getSimpleName();
    public static final String DATABASE_NAME = "mydatabase";
    public static final int DATABASE_VERSION = 4;
    public static final String TABLE_NAME = "weather";
    public static final String CITY_COLUMN = "city";
    public static final String TEMPERATURE_MIN_COLUMN = "temperature_min";
    public static final String TEMPERATURE_MAX_COLUMN = "temperature_max";
    public static final String DATETIME_COLUMN = "datetime";
    public static final String WEATHER_COLUMN = "weather";
    public static final String IMAGE_COLUMN = "icon";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "create table "
                + TABLE_NAME
                + " (" + BaseColumns._ID + " integer primary key autoincrement, "
                + CITY_COLUMN + " text not null, "
                + TEMPERATURE_MIN_COLUMN + " integer not null, "
                + TEMPERATURE_MAX_COLUMN + " integer not null, "
                + WEATHER_COLUMN + " text not null,"
                + DATETIME_COLUMN + " integer not null,"
                + IMAGE_COLUMN + " blob not null);";
        db.execSQL(CREATE_TABLE);
        Log.d(TAG, "Create new table database");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        Log.d(TAG, "Update " + oldVersion + " version to " + newVersion + " version");
    }

    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public void showDataInLog(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()){
            int idColIndex = cursor.getColumnIndex(BaseColumns._ID);
            int cityColIndex = cursor.getColumnIndex(CITY_COLUMN);
            int temperatureMinColIndex = cursor.getColumnIndex(TEMPERATURE_MIN_COLUMN);
            int temperatureMaxColIndex = cursor.getColumnIndex(TEMPERATURE_MAX_COLUMN);
            int weatherColIndex = cursor.getColumnIndex(WEATHER_COLUMN);
            int dateTimeColIndex = cursor.getColumnIndex(DATETIME_COLUMN);
            int imageColIndex = cursor.getColumnIndex(IMAGE_COLUMN);
            do {
                Log.d(TAG, "id = " + cursor.getInt(idColIndex)
                        + "; City = " + cursor.getString(cityColIndex)
                        + "; temperature_min = " + cursor.getInt(temperatureMinColIndex)
                        + "; temperature_max = " + cursor.getInt(temperatureMaxColIndex)
                        + "; weather = " + cursor.getString(weatherColIndex)
                        + "; dateTime = " + cursor.getLong(dateTimeColIndex)
                        + "; imageBlob = " + cursor.getBlob(imageColIndex));
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "Database is null");
        }
        cursor.close();
        db.close();
    }

    public int getCountRows(){
        int count;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();
        count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}
