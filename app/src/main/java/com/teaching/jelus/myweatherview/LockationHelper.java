package com.teaching.jelus.myweatherview;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import static android.content.Context.LOCATION_SERVICE;

public class LockationHelper {
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private final String TAG = "LockationHelper";
    private Location mLocation;
    private double latitude;
    private double longitude;

    public LockationHelper(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled){
            context.startActivity(new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        } else{
            if (isGPSEnabled){
                choseLocationUpdate(context, LocationManager.GPS_PROVIDER);
            } else{
                choseLocationUpdate(context, LocationManager.NETWORK_PROVIDER);
            }
        }
        latitude = mLocation.getLatitude();
        longitude = mLocation.getLongitude();
        Log.d(TAG, "GPS enabled: " + isGPSEnabled
                + " Network enabled: " + isNetworkEnabled
                + " Latitude: " + latitude
                + " Longitude:" + longitude);
    }

    private void choseLocationUpdate(Context context, String provider) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(
                provider,
                0,
                1000,
                mLocationListener);
        mLocation = mLocationManager.getLastKnownLocation(provider);
    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}