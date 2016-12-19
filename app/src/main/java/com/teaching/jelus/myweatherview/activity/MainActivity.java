package com.teaching.jelus.myweatherview.activity;

import android.os.Bundle;
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
import com.teaching.jelus.myweatherview.fragment.LocationFragment;
import com.teaching.jelus.myweatherview.fragment.WeatherFragment;
import com.teaching.jelus.myweatherview.task.ReceivingDataTask;
import com.teaching.jelus.myweatherview.util.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ExecutorService;

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
        Fragment locationFragment = getSupportFragmentManager().findFragmentByTag(LocationFragment.TAG);
        if (locationFragment != null && locationFragment.isAdded()) {
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
                EventBus.getDefault().post(new DataEvent(MessageType.UPDATE_DATA, null));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_weather:
                replaceFragment(new WeatherFragment(), WeatherFragment.TAG, true);
                backToWeatherFragment();
                updateItemMenuVisible();
                break;
            case R.id.nav_location:
                replaceFragment(new LocationFragment(), LocationFragment.TAG, true);
                updateItemMenuVisible();
                break;
        }

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
            case UPDATE_DATA:
                replaceFragment(new WeatherFragment(), WeatherFragment.TAG, false);
                mNavigationView.getMenu().getItem(0).setChecked(true);
                updateItemMenuVisible();
                receiveData();
                break;
        }
    }

    private void receiveData() {
        if (NetworkUtils.isConnected(getApplicationContext())) {
            mPool.submit(new Runnable() {
                @Override
                public void run() {
                    ReceivingDataTask receivingDataTask = new ReceivingDataTask(getApplicationContext());
                    receivingDataTask.method();
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
        Fragment weatherFragment = getSupportFragmentManager().findFragmentByTag(WeatherFragment.TAG);
        Fragment locationFragment = getSupportFragmentManager().findFragmentByTag(LocationFragment.TAG);
        if (weatherFragment != null && weatherFragment.isAdded()) {
            mItemUpdate.setVisible(true);
        } else if (locationFragment != null && locationFragment.isAdded()) {
            mItemUpdate.setVisible(false);
        }
        Log.d(TAG, "updateItemMenuVisible");
    }
}

