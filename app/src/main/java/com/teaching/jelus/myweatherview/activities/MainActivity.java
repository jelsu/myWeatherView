package com.teaching.jelus.myweatherview.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.teaching.jelus.myweatherview.DataEvent;
import com.teaching.jelus.myweatherview.MyApp;
import com.teaching.jelus.myweatherview.R;
import com.teaching.jelus.myweatherview.fragments.LocationFragment;
import com.teaching.jelus.myweatherview.fragments.ProgressFragment;
import com.teaching.jelus.myweatherview.fragments.WeatherFragment;
import com.teaching.jelus.myweatherview.tasks.ReceivingDataTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final String CITY_NAME = "city_name";
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private ProgressFragment mProgressFragment;
    private WeatherFragment mWeatherFragment;
    private LocationFragment mLocationFragment;
    private MenuItem mItemLocation;
    private MenuItem mItemUpdate;
    private MenuItem mItemBack;
    private SharedPreferences mPreferences;
    private String preferCityName;
    private ExecutorService mPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container);
        mPool = (ExecutorService) getLastCustomNonConfigurationInstance();
        if (mPool == null) {
            mFragmentManager = getSupportFragmentManager();
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mProgressFragment = new ProgressFragment();
            mWeatherFragment = new WeatherFragment();
            mLocationFragment = new LocationFragment();
            mFragmentTransaction.add(R.id.container, mProgressFragment);
            mFragmentTransaction.commit();
            mPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
            preferCityName = mPreferences.getString(CITY_NAME, "");
            mPool = MyApp.getPool();
            receiveData(preferCityName);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        mPool.shutdown();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mPool;
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
        if (mProgressFragment != null && mProgressFragment.isAdded()){
            mItemLocation.setVisible(false);
            mItemUpdate.setVisible(false);
            mItemBack.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mLocationFragment.isAdded()){
            mItemLocation.setVisible(true);
            mItemUpdate.setVisible(true);
            mItemBack.setVisible(false);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        switch (id){
            case R.id.menu_item_location:
                mFragmentTransaction.addToBackStack(null);
                mFragmentTransaction.replace(R.id.container, mLocationFragment);
                mFragmentTransaction.commit();
                mItemLocation.setVisible(false);
                mItemUpdate.setVisible(false);
                mItemBack.setVisible(true);
                return true;
            case R.id.menu_item_update:
                mFragmentTransaction.replace(R.id.container, mProgressFragment);
                mFragmentTransaction.commit();
                mItemLocation.setVisible(false);
                mItemUpdate.setVisible(false);
                mItemBack.setVisible(false);
                receiveData(null);
                return true;
            case R.id.menu_item_back:
                mFragmentTransaction.replace(R.id.container, mWeatherFragment);
                mFragmentTransaction.commit();
                mItemLocation.setVisible(true);
                mItemUpdate.setVisible(true);
                mItemBack.setVisible(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataEvent data) {
        switch (data.getMessageType()){
            case "Receive Data":
                mItemLocation.setVisible(true);
                mItemUpdate.setVisible(true);
                mItemBack.setVisible(false);
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.replace(R.id.container, mWeatherFragment);
                mFragmentTransaction.commit();
                Toast.makeText(getApplicationContext(),
                        data.getMessage(),
                        Toast.LENGTH_LONG).show();
                break;
            case "Update request":
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.replace(R.id.container, mProgressFragment);
                mFragmentTransaction.commit();
                mItemLocation.setVisible(false);
                mItemUpdate.setVisible(false);
                mItemBack.setVisible(false);
                receiveData(data.getMessage());
                break;
        }
    }

    private void receiveData(String cityName) {
        if (MyApp.isConnect()) {
            mPool = Executors.newCachedThreadPool();
            mPool.submit(new ReceivingDataTask(cityName));
            mPool.shutdown();
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "receiveData method completed");
    }

}

