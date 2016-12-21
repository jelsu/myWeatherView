package com.teaching.jelus.myweatherview.fragment;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teaching.jelus.myweatherview.DataEvent;
import com.teaching.jelus.myweatherview.R;
import com.teaching.jelus.myweatherview.adapter.RecyclerAdapter;
import com.teaching.jelus.myweatherview.helper.DatabaseHelper;
import com.teaching.jelus.myweatherview.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.CITY_COLUMN;
import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.DATE_COLUMN;
import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.DESCRIPTION_COLUMN;
import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.IMAGE_COLUMN;
import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.TABLE_NAME;
import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.TEMPERATURE_MAX_COLUMN;
import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.TEMPERATURE_MIN_COLUMN;

@SuppressWarnings("WrongConstant")
public class WeatherFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = WeatherFragment.class.getSimpleName();
    private final int LOADER_ID = 1;
    private final int CITY_NAME_UPDATE = 1;
    private final int TEMP_UPDATE = 2;
    private final int DESCRIPTION_UPDATE = 3;
    private final int DATE_UPDATE = 4;
    private final int IMAGE_UPDATE = 5;

    private TextView mTempTextView;
    private TextView mCityNameTextView;
    private TextView mDescriptionTextView;
    private ImageView mWeatherImageView;
    private RecyclerAdapter mRecyclerAdapter;
    private TextView mDateTimeTextView;
    private DatabaseHelper mDatabaseHelper;
    private FrameLayout mProgressFragment;
    private RelativeLayout mDataFragment;
    private android.os.Handler mHandler;

    static class MyCursorLoader extends CursorLoader {
        DatabaseHelper db;
        public MyCursorLoader(Context context, DatabaseHelper db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = db.getForecastData();
            return cursor;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        mProgressFragment = (FrameLayout) view.findViewById(R.id.fragment_progress);
        mDataFragment = (RelativeLayout) view.findViewById(R.id.fragment_data);
        mCityNameTextView = (TextView) view.findViewById(R.id.text_city_name);
        mTempTextView = (TextView) view.findViewById(R.id.text_temperature);
        mDescriptionTextView = (TextView) view.findViewById(R.id.text_weather_description);
        mDateTimeTextView = (TextView) view.findViewById(R.id.text_weather_date);
        mWeatherImageView = (ImageView) view.findViewById(R.id.image_weather);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_forecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerAdapter = new RecyclerAdapter(getActivity(), null);
        recyclerView.setAdapter(mRecyclerAdapter);
        mDatabaseHelper = new DatabaseHelper(getActivity());
        mHandler = new android.os.Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CITY_NAME_UPDATE:
                        mCityNameTextView.setText((String) msg.obj);
                        break;
                    case TEMP_UPDATE:
                        String str = msg.obj + "°";
                        mTempTextView.setText(str);
                        break;
                    case DESCRIPTION_UPDATE:
                        mDescriptionTextView.setText((String) msg.obj);
                        break;
                    case DATE_UPDATE:
                        mDateTimeTextView.setText((String) msg.obj);
                        break;
                    case IMAGE_UPDATE:
                        mWeatherImageView.setImageBitmap((Bitmap) msg.obj);
                        break;
                }
            }
        };
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(getContext(), mDatabaseHelper);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mRecyclerAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerAdapter.swapCursor(null);
    }

    private void updateUI(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
                updateCurrWeatherWidgets(db);
                //TODO check this method correctness
                getLoaderManager().getLoader(LOADER_ID).forceLoad();
                Log.d(TAG, "update UI completed");
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataEvent data) {
        switch (data.getType()) {
            case RECEIVE_DATA:
                updateUI();
                mProgressFragment.setVisibility(View.GONE);
                mDataFragment.setVisibility(View.VISIBLE);
                break;
            case ALL_DATA_UPDATE:
                mProgressFragment.setVisibility(View.VISIBLE);
                mDataFragment.setVisibility(View.GONE);
                break;
            case BACK:
                updateUI();
                mProgressFragment.setVisibility(View.GONE);
                mDataFragment.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateCurrWeatherWidgets(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()){
            int cityColIndex = cursor.getColumnIndex(CITY_COLUMN);
            int tempMinColIndex = cursor.getColumnIndex(TEMPERATURE_MIN_COLUMN);
            int tempMaxColIndex = cursor.getColumnIndex(TEMPERATURE_MAX_COLUMN);
            int descriptionColIndex = cursor.getColumnIndex(DESCRIPTION_COLUMN);
            int dateColIndex = cursor.getColumnIndex(DATE_COLUMN);
            int imageColIndex = cursor.getColumnIndex(IMAGE_COLUMN);
            String cityName = cursor.getString(cityColIndex);
            int tempMin = cursor.getInt(tempMinColIndex);
            int tempMax = cursor.getInt(tempMaxColIndex);
            String averageTemp = String.valueOf(Math.round((double) (tempMax
                    + tempMin) / 2));
            String description = cursor.getString(descriptionColIndex);
            long unixDate = cursor.getLong(dateColIndex);
            long lastUpdateTime = Utils.getDifferenceBetweenDates(unixDate * 1000);
            String lastUpdateStr = getLastUpdateString(lastUpdateTime);
            Bitmap image = Utils.convertByteArrayToBitmap(cursor.getBlob(imageColIndex));
            sendHandlerMessage(CITY_NAME_UPDATE, cityName);
            sendHandlerMessage(TEMP_UPDATE, averageTemp);
            sendHandlerMessage(DESCRIPTION_UPDATE, description);
            sendHandlerMessage(DATE_UPDATE, lastUpdateStr);
            sendHandlerMessage(IMAGE_UPDATE, image);
        } else {
            Log.d(TAG, "Database is null");
        }
        cursor.close();
    }

    private void sendHandlerMessage(int what, Object obj) {
        Message msg;
        msg = mHandler.obtainMessage(what, obj);
        mHandler.sendMessage(msg);
    }

    private String getLastUpdateString(long updateTime){
        if (updateTime >= 60){
            int hours = (int) updateTime / 60;
            if (hours > 24){
                return "Updated a few days ago";
            }
            return "Updated " + hours + " hours ago";
        }
        return "Updated " + updateTime + " minutes ago";
    }
}
