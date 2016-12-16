package com.teaching.jelus.myweatherview;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    private static final String TAG = Settings.class.getSimpleName();
    private static final String KEY_PREFER_CITY_NAME = "prefer_city_name";
    private static final String KEY_CITY_NAME = "city_name";
    private static final String KEY_LOCATE = "locate";
    private static final String PREF_NAME = "preferences";
    private SharedPreferences mPreferences;

    public Settings(Context context) {
        mPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (mPreferences.contains(KEY_CITY_NAME)) {
            mPreferences.edit().remove(KEY_CITY_NAME).apply();
        }
    }

    public String getPreferCityNameValue() {
        return mPreferences.getString(KEY_PREFER_CITY_NAME, null);
    }

    public void setPreferCityNameValue(String cityName) {
        mPreferences.edit().putString(KEY_PREFER_CITY_NAME, cityName).apply();
    }

    public void removePreferCityNameValue() {
        mPreferences.edit().remove(KEY_PREFER_CITY_NAME).apply();
    }

    public boolean isContainPreferCityName() {
        return mPreferences.contains(KEY_PREFER_CITY_NAME);
    }

    public String getCityNameValue() {
        return mPreferences.getString(KEY_CITY_NAME, null);
    }

    public void setCityNameValue(String cityName) {
        mPreferences.edit().putString(KEY_CITY_NAME, cityName).apply();
    }

    public void removeCityNameValue() {
        mPreferences.edit().remove(KEY_CITY_NAME).apply();
    }

    public boolean isContainCityName() {
        return mPreferences.contains(KEY_CITY_NAME);
    }

    public boolean getLocateValue() {
        return mPreferences.getBoolean(KEY_LOCATE, false);
    }

    public void setLocateValue(boolean locate) {
        mPreferences.edit().putBoolean(KEY_LOCATE, locate).apply();
    }
}
