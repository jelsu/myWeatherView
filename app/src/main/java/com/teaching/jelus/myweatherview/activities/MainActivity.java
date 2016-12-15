package com.teaching.jelus.myweatherview.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.teaching.jelus.myweatherview.DataEvent;
import com.teaching.jelus.myweatherview.MyApp;
import com.teaching.jelus.myweatherview.NetworkUtils;
import com.teaching.jelus.myweatherview.R;
import com.teaching.jelus.myweatherview.fragments.LocationFragment;
import com.teaching.jelus.myweatherview.fragments.WeatherFragment;
import com.teaching.jelus.myweatherview.tasks.ReceivingDataTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ExecutorService;

import static com.teaching.jelus.myweatherview.MessageType.BACK;
import static com.teaching.jelus.myweatherview.MessageType.UPDATE_DATA;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final String CITY_NAME = "city_name";
    private FragmentManager mFragmentManager;
    private WeatherFragment mWeatherFragment;
    private LocationFragment mLocationFragment;
    private MenuItem mItemLocation;
    private MenuItem mItemUpdate;
    private MenuItem mItemBack;
    private SharedPreferences mPreferences;
    private String mPreferCityName;
    private ExecutorService mPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        mWeatherFragment = new WeatherFragment();
        mLocationFragment = new LocationFragment();
        replaceFragment(mWeatherFragment, false);
        mPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        mPreferCityName = mPreferences.getString(CITY_NAME, "");
        mPool = MyApp.getPool();
        receiveData(mPreferCityName);
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
        mItemLocation = menu.findItem(R.id.menu_item_location);
        mItemUpdate = menu.findItem(R.id.menu_item_update);
        mItemBack = menu.findItem(R.id.menu_item_back);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mWeatherFragment != null && mWeatherFragment.isAdded()){
            menuItemsVisibilitySettings(true, true, false);
        }
        if (mLocationFragment != null && mLocationFragment.isAdded()){
            menuItemsVisibilitySettings(false, false, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mLocationFragment.isAdded()){
            menuItemsVisibilitySettings(true, true, false);
            backToWeatherFragment();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_item_location:
                replaceFragment(mLocationFragment, true);
                menuItemsVisibilitySettings(false, false, true);
                return true;
            case R.id.menu_item_update:
                replaceFragment(mWeatherFragment, false);
                EventBus.getDefault().post(new DataEvent(UPDATE_DATA, ""));
                menuItemsVisibilitySettings(false, false, false);
                mPreferCityName = mPreferences.getString(CITY_NAME, null);
                receiveData(mPreferCityName);
                return true;
            case R.id.menu_item_back:
                replaceFragment(mWeatherFragment, false);
                backToWeatherFragment();
                menuItemsVisibilitySettings(true, true, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataEvent data) {
        switch (data.getType()){
            case RECEIVE_DATA:
                menuItemsVisibilitySettings(true, true, false);
                /*replaceFragment(mWeatherFragment, false);*/
                Toast.makeText(getApplicationContext(),
                        data.getMessage(),
                        Toast.LENGTH_LONG).show();
                break;
            case UPDATE_DATA:
                /*replaceFragment(mProgressFragment, false);*/
                menuItemsVisibilitySettings(false, false, false);
                receiveData(data.getMessage());
                break;
        }
    }

    private void receiveData(String cityName) {
        if (NetworkUtils.isConnected(getApplicationContext())) {
            mPool.submit(new Runnable() {
                @Override
                public void run() {
                    ReceivingDataTask receivingDataTask = new ReceivingDataTask(getApplicationContext(), mPreferCityName);
                    receivingDataTask.method();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(),
                    "No internet connection", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "receiveData method completed");
    }

    private void replaceFragment(Fragment fragment, boolean addToBackStack){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if (addToBackStack){
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    private void menuItemsVisibilitySettings(boolean itemLocationVisible,
                                             boolean itemUpdateVisible,
                                             boolean itemBackVisible){
        mItemLocation.setVisible(itemLocationVisible);
        mItemUpdate.setVisible(itemUpdateVisible);
        mItemBack.setVisible(itemBackVisible);
    }

    private void backToWeatherFragment(){
        //TODO find correct solution
        mPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    EventBus.getDefault().post(new DataEvent(BACK, null));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

