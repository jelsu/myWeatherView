package com.teaching.jelus.myweatherview.activity;

import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.teaching.jelus.myweatherview.DataEvent;
import com.teaching.jelus.myweatherview.MessageType;
import com.teaching.jelus.myweatherview.MyApp;
import com.teaching.jelus.myweatherview.R;
import com.teaching.jelus.myweatherview.Settings;
import com.teaching.jelus.myweatherview.fragment.LocationFragment;
import com.teaching.jelus.myweatherview.fragment.WeatherFragment;
import com.teaching.jelus.myweatherview.helper.LocationHelper;
import com.teaching.jelus.myweatherview.task.ReceivingDataTask;
import com.teaching.jelus.myweatherview.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.net.URL;
import java.util.concurrent.ExecutorService;

import static com.teaching.jelus.myweatherview.MessageType.RECEIVE_DATA;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private MenuItem mItemUpdate;
    private NavigationView mNavigationView;
    private ExecutorService mPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        replaceFragment(new WeatherFragment(), WeatherFragment.TAG, false);
        mNavigationView.getMenu().getItem(0).setChecked(true);
        mPool = MyApp.getPool();
        receiveData();
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
            mNavigationView.getMenu().getItem(0).setChecked(true);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        updateItemMenuVisible();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_item_update:
                EventBus.getDefault().post(new DataEvent(MessageType.ALL_DATA_UPDATE, null));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_weather:
                if (!isFragmentAdded(WeatherFragment.TAG)) {
                    replaceFragment(new WeatherFragment(), WeatherFragment.TAG, false);
                    backToWeatherFragment();
                }
                break;
            case R.id.nav_location:
                if (!isFragmentAdded(LocationFragment.TAG)) {
                    replaceFragment(new LocationFragment(), LocationFragment.TAG, true);
                }
                break;
        }
        updateItemMenuVisible();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataEvent data) {
        switch (data.getType()){
            case RECEIVE_DATA:
                Toast.makeText(getApplicationContext(),
                        data.getMessage(),
                        Toast.LENGTH_LONG).show();
                break;
            case ALL_DATA_UPDATE:
                replaceFragment(new WeatherFragment(), WeatherFragment.TAG, false);
                mNavigationView.getMenu().getItem(0).setChecked(true);
                updateItemMenuVisible();
                receiveData();
                break;
        }
    }

    private void receiveData() {
        if (Utils.isConnected(getApplicationContext())) {
            mPool.submit(new Runnable() {
                @Override
                public void run() {
                    //DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                    LocationHelper locationHelper = new LocationHelper(getApplicationContext());
                    try {
                        if (Looper.myLooper() == null)
                        {
                            Looper.prepare();
                        }
                        locationHelper.start();
                        URL currWeatherUrl = ReceivingDataTask.getUrl("weather", locationHelper.getLatitude(), locationHelper.getLongitude());
                        URL forecastUrl = ReceivingDataTask.getUrl("forecast/daily", locationHelper.getLatitude(), locationHelper.getLongitude());
                        Settings settings = MyApp.getSettings();
                        settings.removeCityNameValue();
                        String currWeatherStr = ReceivingDataTask.getStringFromUrl(currWeatherUrl);
                        String forecastStr = ReceivingDataTask.getStringFromUrl(forecastUrl);
                        JSONObject currWeatherJsonData = ReceivingDataTask.getJsonFromStr(currWeatherStr);
                        JSONObject forecastJsonData = ReceivingDataTask.getJsonFromStr(forecastStr);
                        locationHelper.stop();
                        if (ReceivingDataTask.isDataCorrect(currWeatherJsonData)
                                && ReceivingDataTask.isDataCorrect(forecastJsonData)) {
                            ReceivingDataTask.saveCurrWeatherDataInDb(currWeatherJsonData);
                            ReceivingDataTask.saveForecastDataInDb(forecastJsonData);
                            MyApp.getDatabaseHelper().showDataInLog();
                            EventBus.getDefault().post(new DataEvent(RECEIVE_DATA,
                                    "Data successfully updated"));
                        } else {
                            EventBus.getDefault().post(new DataEvent(RECEIVE_DATA,
                                    "Receiving data error"));
                        }
                        //databaseHelper.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        EventBus.getDefault().post(new DataEvent(RECEIVE_DATA,
                                "Receiving data error"));
                        locationHelper.stop();
                        //databaseHelper.close();
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(),
                    "No internet connection", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "receiveData method completed");
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
        Log.d(TAG, "updateItemMenuVisible");
    }

    private boolean isFragmentAdded(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        return fragment != null && fragment.isAdded();
    }

}

