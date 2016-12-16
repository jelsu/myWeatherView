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

import static com.teaching.jelus.myweatherview.MessageType.BACK;
import static com.teaching.jelus.myweatherview.MessageType.UPDATE_DATA;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private FragmentManager mFragmentManager;
    private WeatherFragment mWeatherFragment;
    private LocationFragment mLocationFragment;
    private MenuItem mItemLocation;
    private MenuItem mItemUpdate;
    private MenuItem mItemBack;
    private ExecutorService mPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFragmentManager = getSupportFragmentManager();
        mWeatherFragment = new WeatherFragment();
        mLocationFragment = new LocationFragment();
        replaceFragment(mWeatherFragment, false);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                EventBus.getDefault().post(new DataEvent(UPDATE_DATA, null));
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                replaceFragment(mWeatherFragment, false);
                menuItemsVisibilitySettings(false, false, false);
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

    private void replaceFragment(Fragment fragment, boolean addToBackStack){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if (addToBackStack){
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.replace(R.id.fragment_container, fragment);
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

