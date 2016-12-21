package com.teaching.jelus.myweatherview.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teaching.jelus.myweatherview.R;
import com.teaching.jelus.myweatherview.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.DATE_COLUMN;
import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.DESCRIPTION_COLUMN;
import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.IMAGE_COLUMN;
import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.TEMPERATURE_MAX_COLUMN;
import static com.teaching.jelus.myweatherview.helper.DatabaseHelper.TEMPERATURE_MIN_COLUMN;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private Context mContext;
    private Cursor mCursor;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mForecastTempeTextView;
        private TextView mForecastDescriptionTextView;
        private TextView mForecastDataTextView;
        private ImageView mForecastImageView;


        public ViewHolder(View v) {
            super(v);
            mForecastTempeTextView = (TextView) v.findViewById(
                    R.id.text_forecast_temperature);
            mForecastDescriptionTextView = (TextView) v.findViewById(
                    R.id.text_forecast_weather_description);
            mForecastDataTextView = (TextView) v.findViewById(
                    R.id.text_forecast_date);
            mForecastImageView = (ImageView) v.findViewById(R.id.image_forecast_weather);
        }
    }

    public RecyclerAdapter(Activity context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        int tempMinColIndex = mCursor.getColumnIndex(TEMPERATURE_MIN_COLUMN);
        int tempMaxColIndex = mCursor.getColumnIndex(TEMPERATURE_MAX_COLUMN);
        int descriptionColIndex = mCursor.getColumnIndex(DESCRIPTION_COLUMN);
        int dateColIndex = mCursor.getColumnIndex(DATE_COLUMN);
        int imageColIndex = mCursor.getColumnIndex(IMAGE_COLUMN);
        int tempMin = mCursor.getInt(tempMinColIndex);
        int tempMax = mCursor.getInt(tempMaxColIndex);
        String description = mCursor.getString(descriptionColIndex);
        String date = getStringDate(Utils.convertUnixTimeToDate(mCursor.getLong(dateColIndex)));
        Bitmap image = Utils.convertByteArrayToBitmap(mCursor.getBlob(imageColIndex));
        holder.mForecastTempeTextView.setText(String.valueOf(tempMax) + "° / "
                + String.valueOf(tempMin) + "°");
        holder.mForecastDescriptionTextView.setText(description);
        holder.mForecastDataTextView.setText(date);
        holder.mForecastImageView.setImageBitmap(image);
    }

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public Cursor swapCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        this.mCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    private String getStringDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM");
        Date currentDate = new Date();
        long minDifference = Utils.getDifferenceBetweenDates(date.getTime()) - 720;
        if (dateFormat.format(date).equals(dateFormat.format(currentDate))){
            return "Today";
        } else if (minDifference <= 1440){
            return "Tomorrow";
        }
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date);
    }

}
