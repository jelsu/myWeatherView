package com.teaching.jelus.myweatherview.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.teaching.jelus.myweatherview.DataEvent;
import com.teaching.jelus.myweatherview.MessageType;
import com.teaching.jelus.myweatherview.MyApp;
import com.teaching.jelus.myweatherview.R;
import com.teaching.jelus.myweatherview.Settings;
import com.teaching.jelus.myweatherview.fragment.LocationFragment;
import com.teaching.jelus.myweatherview.fragment.WeatherFragment;
import com.teaching.jelus.myweatherview.task.ReceivingDataTask;
import com.teaching.jelus.myweatherview.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.net.URL;
import java.util.concurrent.ExecutorService;

import static com.teaching.jelus.myweatherview.MessageType.ALL_DATA_UPDATE;
import static com.teaching.jelus.myweatherview.MessageType.RECEIVE_DATA;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_PERMS_REQUEST_CODE = 1;
    private MenuItem mItemUpdate;
    private Drawer mNavigationDrawer;
    private ExecutorService mPool;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setTitle("");

        initializeNavigatioanDrawer(toolbar);

        replaceFragment(new WeatherFragment(), WeatherFragment.TAG, false);
        mPool = MyApp.getPool();
        receiveData();
    }

    private void initializeNavigatioanDrawer(Toolbar toolbar) {
        mNavigationDrawer = new DrawerBuilder(this)
                .withRootView(R.id.drawer_container)
                .withToolbar(toolbar)
                .withDisplayBelowStatusBar(false)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_weather)
                                .withIcon(R.drawable.ic_wb_sunny_black_24dp)
                                .withIdentifier(0),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_location)
                                .withIcon(R.drawable.ic_edit_location_black_24dp)
                                .withIdentifier(1)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        int identifier = (int) drawerItem.getIdentifier();
                        switch (identifier) {
                            case 0:
                                if (!isFragmentAdded(WeatherFragment.TAG)) {
                                    replaceFragment(new WeatherFragment(),
                                            WeatherFragment.TAG, false);
                                    backToWeatherFragment();
                                }
                                break;
                            case 1:
                                if (!isFragmentAdded(LocationFragment.TAG)) {
                                    replaceFragment(new LocationFragment(),
                                            LocationFragment.TAG, true);
                                }
                                break;
                        }
                        updateItemMenuVisible();
                        return false;
                    }
                })
                .withSelectedItem(0)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mItemUpdate = menu.findItem(R.id.menu_item_update);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (isFragmentAdded(LocationFragment.TAG)) {
            backToWeatherFragment();
            mNavigationDrawer.setSelection(0, false);
        }
        if (mNavigationDrawer != null && mNavigationDrawer.isDrawerOpen()) {
            mNavigationDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
        updateItemMenuVisible();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_item_update:
                EventBus.getDefault().post(new DataEvent(ALL_DATA_UPDATE, null));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMS_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                    Toast.makeText(getApplicationContext(),
                            "Permission was granted",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataEvent data) {
        switch (data.getType()) {
            case RECEIVE_DATA:
                Toast.makeText(getApplicationContext(),
                        data.getMessage(),
                        Toast.LENGTH_SHORT).show();
                mItemUpdate.setVisible(true);
                break;
            case ALL_DATA_UPDATE:
                replaceFragment(new WeatherFragment(), WeatherFragment.TAG, false);
                mNavigationDrawer.setSelection(0, false);
                receiveData();
                mItemUpdate.setVisible(false);
                break;
        }
    }

    private void receiveData() {
        if (Utils.isConnected(getApplicationContext())) {
            updateLocationWrapper();
            mPool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL currWeatherUrl = ReceivingDataTask.getUrl("weather", mLocation);
                        URL forecastUrl = ReceivingDataTask.getUrl("forecast/daily", mLocation);

                        Settings settings = MyApp.getSettings();
                        settings.removeCityNameValue();

                        String currWeatherStr = ReceivingDataTask.getStrFromUrl(currWeatherUrl);
                        String forecastStr = ReceivingDataTask.getStrFromUrl(forecastUrl);

                        JSONObject currWeatherJsonData = ReceivingDataTask
                                .getJsonFromStr(currWeatherStr);
                        JSONObject forecastJsonData = ReceivingDataTask
                                .getJsonFromStr(forecastStr);

                        ReceivingDataTask.checkAndSaveDataToDb(currWeatherJsonData,
                                forecastJsonData);
                    } catch (Exception e) {
                        e.printStackTrace();
                        EventBus.getDefault().post(new DataEvent(RECEIVE_DATA,
                                "Receiving data error"));
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(),
                    "No internet connection", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "receiveData method completed");
    }

    //TODO find correct solution
    private void updateLocationWrapper() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMS_REQUEST_CODE);
        } else {
            updateLocation();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void updateLocation() {
        final int LOCATION_REFRESH_TIME = 1000;
        final int LOCATION_REFRESH_DISTANCE = 5;
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE,
                locationListener);
        mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    private void replaceFragment(Fragment fragment, String tag, boolean addToBackStack){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (addToBackStack){
            fragmentTransaction.addToBackStack(tag);
        }
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
        if (mItemUpdate != null) {
            updateItemMenuVisible();
        }
    }

    private void backToWeatherFragment(){
        //TODO find correct solution
        mPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    EventBus.getDefault().post(new DataEvent(MessageType.BACK, null));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateItemMenuVisible() {
        if (isFragmentAdded(WeatherFragment.TAG)) {
            mItemUpdate.setVisible(true);
        } else if (isFragmentAdded(LocationFragment.TAG)) {
            mItemUpdate.setVisible(false);
        }
    }

    private boolean isFragmentAdded(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        return fragment != null && fragment.isAdded();
    }

}

