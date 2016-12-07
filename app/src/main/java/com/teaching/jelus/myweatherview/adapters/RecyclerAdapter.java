package com.teaching.jelus.myweatherview.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.teaching.jelus.myweatherview.ForecastData;
import com.teaching.jelus.myweatherview.R;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private ArrayList<ForecastData> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mForecastTemperatureTextView;
        private TextView mForecastWeatherDescriptionTextView;
        private TextView mForecastDataTextView;
        private ImageView mForecastImageView;


        public ViewHolder(View v) {
            super(v);
            mForecastTemperatureTextView = (TextView) v.findViewById(
                    R.id.text_forecast_temperature);
            mForecastWeatherDescriptionTextView = (TextView) v.findViewById(
                    R.id.text_forecast_weather_description);
            mForecastDataTextView = (TextView) v.findViewById(
                    R.id.text_forecast_date);
            mForecastImageView = (ImageView) v.findViewById(R.id.image_forecast_weather);
        }
    }

    public RecyclerAdapter(ArrayList<ForecastData> forecastData) {
        mDataset = forecastData;
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
        ForecastData forecastData = mDataset.get(position);
        holder.mForecastTemperatureTextView.setText(String.valueOf(forecastData.getTemperatureMax())
                + "° / "
                + String.valueOf(forecastData.getTemperatureMin())
                + "°");
        holder.mForecastWeatherDescriptionTextView.setText(forecastData.getWeatherDescription());
        holder.mForecastDataTextView.setText(forecastData.getData());
        holder.mForecastImageView.setImageBitmap(forecastData.getImage());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}