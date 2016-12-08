package com.teaching.jelus.myweatherview.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {
    private static final String TAG = DatabaseHelper.class.getSimpleName();
    public static final String DATABASE_NAME = "db";
    public static final int DATABASE_VERSION = 4;
    public static final String TABLE_NAME = "weather";
    public static final String ID_COLUMN = BaseColumns._ID;
    public static final String CITY_COLUMN = "city";
    public static final String TEMPERATURE_MIN_COLUMN = "temperature_min";
    public static final String TEMPERATURE_MAX_COLUMN = "temperature_max";
    public static final String DATE_COLUMN = "datetime";
    public static final String DESCRIPTION_COLUMN = "weather";
    public static final String IMAGE_COLUMN = "image";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "create table "
                + TABLE_NAME
                + " (" + ID_COLUMN + " integer primary key, "
                + CITY_COLUMN + " text not null, "
                + TEMPERATURE_MIN_COLUMN + " integer not null, "
                + TEMPERATURE_MAX_COLUMN + " integer not null, "
                + DESCRIPTION_COLUMN + " text not null,"
                + DATE_COLUMN + " integer not null,"
                + IMAGE_COLUMN + " blob not null);";
        db.execSQL(CREATE_TABLE);
        Log.d(TAG, "Create new table database");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(DROP_TABLE);
        onCreate(db);
        Log.d(TAG, "Update " + oldVersion + " version to " + newVersion + " version");
    }

    public void showDataInLog(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()){
            int idColIndex = cursor.getColumnIndex(ID_COLUMN);
            int cityColIndex = cursor.getColumnIndex(CITY_COLUMN);
            int tempMinColIndex = cursor.getColumnIndex(TEMPERATURE_MIN_COLUMN);
            int tempMaxColIndex = cursor.getColumnIndex(TEMPERATURE_MAX_COLUMN);
            int descriptionColIndex = cursor.getColumnIndex(DESCRIPTION_COLUMN);
            int dateColIndex = cursor.getColumnIndex(DATE_COLUMN);
            int imageColIndex = cursor.getColumnIndex(IMAGE_COLUMN);
            do {
                Log.d(TAG, "id = " + cursor.getInt(idColIndex)
                        + "; City = " + cursor.getString(cityColIndex)
                        + "; temperature_min = " + cursor.getInt(tempMinColIndex)
                        + "; temperature_max = " + cursor.getInt(tempMaxColIndex)
                        + "; description = " + cursor.getString(descriptionColIndex)
                        + "; date = " + cursor.getLong(dateColIndex)
                        + "; imageBlob = " + cursor.getBlob(imageColIndex));
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "Database is null");
        }
        cursor.close();
        db.close();
    }

    public boolean isRecordExists(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        final String QUERY = "Select * from " + TABLE_NAME + " where " + ID_COLUMN + " = " + id;
        Cursor cursor = db.rawQuery(QUERY, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
}
