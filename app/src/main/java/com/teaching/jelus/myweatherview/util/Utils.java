package com.teaching.jelus.myweatherview.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Date;

public final class Utils {
    public static boolean isConnected(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static Bitmap convertByteArrayToBitmap(byte[] data){
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static Date convertUnixTimeToDate(long unixTime){
        return new Date(unixTime * 1000);
    }

    public static long getDifferenceBetweenDates(long date) {
        long millisDifference = Math.abs(System.currentTimeMillis() - date) / 1000;
        return millisDifference / 60;
    }
}
