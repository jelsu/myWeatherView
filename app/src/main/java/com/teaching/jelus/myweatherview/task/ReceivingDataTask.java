package com.teaching.jelus.myweatherview.task;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.teaching.jelus.myweatherview.MyApp;
import com.teaching.jelus.myweatherview.Settings;
import com.teaching.jelus.myweatherview.helper.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.CITY_COLUMN;
import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.ID_COLUMN;

public class ReceivingDataTask {
    private static final String TAG = ReceivingDataTask.class.getSimpleName();

    public static URL getUrl(String requestType, double latitude, double longitude) throws Exception {
        Settings settings = MyApp.getSettings();
        final String BEGINNING_URL = "http://api.openweathermap.org/data/2.5/";
        final String APP_ID = "98fb5e0dcef9e5de3219365edf223805";
        StringBuilder compositeUrl = new StringBuilder(BEGINNING_URL + requestType);
        if (settings.isContainCityName()) {
            compositeUrl.append("?q=" + settings.getCityNameValue());
        } else if (settings.isContainPreferCityName()) {
            compositeUrl.append("?q=" + settings.getPreferCityNameValue());
        } else {
            String coordinateStr = "?lat=" + latitude + "&lon=" + longitude;
            compositeUrl.append(coordinateStr);
        }
        compositeUrl.append("&appid=" + APP_ID);
        compositeUrl.append("&units=metric");
        Log.d(TAG, "Composite URL: " + compositeUrl.toString());
        return new URL(compositeUrl.toString());
    }

    public static JSONObject getJsonFromStr(String str) throws JSONException {
        return new JSONObject(str);
    }

    public static void saveCurrWeatherDataToDb(JSONObject jsonObject) throws Exception{
        final int CURR_RECORD_ID = 1;
        final int SINGLE_POSITION = 0;
        String cityName = jsonObject.getString("name");
        JSONObject main = jsonObject.getJSONObject("main");
        int tempMin = (int) Math.round(main.getDouble("temp_min"));
        int tempMax = (int) Math.round(main.getDouble("temp_max"));
        JSONArray weatherArray = jsonObject.getJSONArray("weather");
        JSONObject weather = (JSONObject) weatherArray.get(SINGLE_POSITION);
        String description = weather.getString("description");
        long date = jsonObject.getLong("dt");
        String iconCode = weather.getString("icon");
        byte[] image = downloadImage(iconCode);
        updateTableRow(CURR_RECORD_ID, cityName, tempMin, tempMax, description, date, image);
        Log.d(TAG, "saveCurrWeatherDataToDb method worked");
    }

    public static void saveForecastDataToDb(JSONObject jsonObject) throws Exception{
        final int SINGLE_POSITION = 0;
        int currRecordId;
        JSONArray list = jsonObject.getJSONArray("list");
        for (int i = 0; i < list.length(); i++)
        {
            JSONObject city = jsonObject.getJSONObject("city");
            String cityName = city.getString("name");
            JSONObject listItem = (JSONObject) list.get(i);
            JSONObject temp = listItem.getJSONObject("temp");
            int tempMin = (int) Math.round(temp.getDouble("min"));
            int tempMax = (int) Math.round(temp.getDouble("max"));
            JSONArray weatherArray = listItem.getJSONArray("weather");
            JSONObject weather = (JSONObject) weatherArray.get(SINGLE_POSITION);
            String description = weather.getString("description");
            long date = listItem.getLong("dt");
            String iconCode = weather.getString("icon");
            byte[] image = downloadImage(iconCode);
            currRecordId = i + 2;
            updateTableRow(currRecordId, cityName, tempMin, tempMax, description, date, image);
        }
        Log.d(TAG, "saveForecastDataToDb method worked");
    }

    private static byte[] downloadImage(String icon){
        final String URL = "http://openweathermap.org/img/w/" + icon + ".png";
        try {
            InputStream in = new java.net.URL(URL).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            byte[] buffer = out.toByteArray();
            return buffer;
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return new byte[0];
    }

    private static void updateTableRow(int id,
                                String cityName,
                                int tempMin,
                                int tempMax,
                                String description,
                                long date,
                                byte[] image){
        DatabaseHelper databaseHelper = MyApp.getDatabaseHelper();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID_COLUMN, id);
        values.put(CITY_COLUMN, cityName);
        values.put(DatabaseHelper.TEMPERATURE_MIN_COLUMN, tempMin);
        values.put(DatabaseHelper.TEMPERATURE_MAX_COLUMN, tempMax);
        values.put(DatabaseHelper.DESCRIPTION_COLUMN, description);
        values.put(DatabaseHelper.DATE_COLUMN, date);
        values.put(DatabaseHelper.IMAGE_COLUMN, image);
        if (databaseHelper.isRecordExists(id)){
            db.update(DatabaseHelper.TABLE_NAME, values, ID_COLUMN + " = " + id, null);
        } else {
            db.insert(DatabaseHelper.TABLE_NAME, null, values);
        }
    }

    public static String getStrFromUrl(URL url) throws IOException {
        HttpURLConnection urlConnection;
        BufferedReader reader;
        InputStream inputStream;
        StringBuffer buffer;
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        inputStream = urlConnection.getInputStream();
        buffer = new StringBuffer();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null){
            buffer.append(line);
        }
        return buffer.toString();
    }

    public static boolean isDataCorrect(JSONObject data) throws JSONException {
        final int OK_STATUS_CODE = 200;
        int cod = data.getInt("cod");
        if (cod == OK_STATUS_CODE){
            return true;
        }
        return false;
    }
}
