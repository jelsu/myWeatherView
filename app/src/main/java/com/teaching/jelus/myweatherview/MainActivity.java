package com.teaching.jelus.myweatherview;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private ProgressFragment mProgressFragment;
    private WeatherFragment mWeatherFragment;
    private MenuItem mItemLockation;
    private MenuItem mItemUpdate;
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
            mFragmentTransaction.add(R.id.container, mProgressFragment);
            mFragmentTransaction.commit();
            mPool = MyApp.getPool();
            receiveData();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        mPool.shutdownNow();
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
        mItemLockation = menu.findItem(R.id.menu_item_lockation);
        mItemUpdate = menu.findItem(R.id.menu_item_update);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_item_lockation:
                return true;
            case R.id.menu_item_update:
                mItemLockation.setEnabled(false);
                mItemUpdate.setEnabled(false);
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.replace(R.id.container, mProgressFragment);
                mFragmentTransaction.commit();
                receiveData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String requst) {
        mItemLockation.setEnabled(true);
        mItemUpdate.setEnabled(true);
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.container, mWeatherFragment);
        mFragmentTransaction.commit();
        switch (requst){
            case "success":
                Toast.makeText(getApplicationContext(),
                        "Data successfully updated", Toast.LENGTH_LONG).show();
                break;
            case "error":
                Toast.makeText(getApplicationContext(),
                        "Receiving data error", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void receiveData() {
        if (isConnect()) {
            mPool = MyApp.getPool();
            mPool = Executors.newSingleThreadExecutor();
            mPool.submit(new ReceivingDataTask());
            mPool.shutdown();
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "receiveData method completed");
    }

    private boolean isConnect(){
        ConnectivityManager connectivityManager = (ConnectivityManager) MyApp
                .getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}

